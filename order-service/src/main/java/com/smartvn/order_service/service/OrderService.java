package com.smartvn.order_service.service;

import com.smartvn.order_service.client.ProductServiceClient;
import com.smartvn.order_service.client.UserServiceClient;
import com.smartvn.order_service.dto.admin.OrderStatsDTO;
import com.smartvn.order_service.dto.admin.RevenueChartDTO;
import com.smartvn.order_service.dto.product.InventoryCheckRequest;
import com.smartvn.order_service.dto.product.ProductDTO;
import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.enums.PaymentMethod;
import com.smartvn.order_service.enums.PaymentStatus;
import com.smartvn.order_service.exceptions.AppException;
import com.smartvn.order_service.model.Cart;
import com.smartvn.order_service.model.CartItem;
import com.smartvn.order_service.model.Order;
import com.smartvn.order_service.model.OrderItem;
import com.smartvn.order_service.repository.CartItemRepository;
import com.smartvn.order_service.repository.CartRepository;
import com.smartvn.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final OrderRepository orderRepository;
    private final CartRepository  cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;

    @Transactional
    public Order placeOrder(
            Long userId,
            Long addressId,
            List<Long> cartItemIds) {
        Boolean isValidAddress = userServiceClient.validateUserAddress(addressId, userId);
        if (!isValidAddress) {
            throw new AppException(
                    "Address does not belong to user or is invalid",
                    HttpStatus.BAD_REQUEST
            );
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException("Cart not found", HttpStatus.NOT_FOUND));

        List<CartItem> selectedItems = cartItemRepository.findAllById(cartItemIds);
        if (selectedItems.isEmpty()) {
            throw new AppException("No valid items found", HttpStatus.BAD_REQUEST);
        }

        boolean allItemsBelongToCart = selectedItems.stream().allMatch(
                item -> item.getCart().getId().equals(cart.getId())
        );
        if (!allItemsBelongToCart) {
            throw new AppException("Some items don't belong to your cart", HttpStatus.FORBIDDEN);
        }

        List<InventoryCheckRequest> inventoryCheckRequests = selectedItems.stream()
                .map(i -> new InventoryCheckRequest(
                        i.getProductId(),
                        i.getSize(),
                        i.getQuantity()
                ))
                .collect(Collectors.toList());

        Map<String, Boolean> stockResults = productServiceClient.batchCheckInventory(inventoryCheckRequests);

        for (InventoryCheckRequest req : inventoryCheckRequests) {
            String key = req.getProductId() + "-" + req.getSize();
            Boolean hasStock = stockResults.get(key);

            if (!hasStock) {
                ProductDTO product = productServiceClient.getProductById(req.getProductId());
                throw new AppException(
                        String.format("Sản phẩm '%s' (size %s) không đủ hàng",
                                product.getTitle(), req.getSize()),
                        HttpStatus.BAD_REQUEST
                );
            }
        }


        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddressId(addressId);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setShippingAddressId(addressId);

        List<OrderItem> orderItems = selectedItems.stream()
                .map(this::convertToOrderItem)
                .collect(Collectors.toList());

        orderItems.forEach(i -> i.setOrder(order));
        order.setOrderItems(orderItems);
        order.calculateTotals();

        Order savedOrder = orderRepository.save(order);

        try {
            productServiceClient.batchReduceInventory(inventoryCheckRequests);
        } catch (Exception e) {
            log.error("Failed to reduce inventory batch: {}", e.getMessage());
            orderRepository.delete(savedOrder);
            throw new AppException(
                    "Không thể xử lý đơn hàng. Vui lòng thử lại.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // ✅ 11. Xóa items đã checkout khỏi cart
        cartItemRepository.deleteAll(selectedItems);

        Cart updatedCart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException("Cart not found", HttpStatus.NOT_FOUND));
        cartService.reCalculateCart(updatedCart);
        cartRepository.save(updatedCart);

        log.info("✅ Order {} created successfully with {} items",
                savedOrder.getId(), orderItems.size());

        return savedOrder;
    }

    @Transactional
    public List<Order> getOrderHistory(Long userId, OrderStatus orderStatus) {
        if(orderStatus!=null) {
            return orderRepository.findByUserIdAndOrderStatus(userId, orderStatus);
        }
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = findOrderById(orderId);

        if(!order.getUserId().equals(userId)) {
            throw new AppException("Unauthorized", HttpStatus.FORBIDDEN);
        }

        // Chỉ cho phép hủy nếu PENDING hoặc CONFIRMED
        if (order.getOrderStatus() != OrderStatus.PENDING &&
                order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(
                    "Cannot cancel order in status: " + order.getOrderStatus(),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Restore inventory
        for (OrderItem item : order.getOrderItems()) {
            InventoryCheckRequest restoreRequest = new InventoryCheckRequest(
                    item.getProductId(),
                    item.getSize(),
                    item.getQuantity()
            );

            try {
                productServiceClient.restoreInventory(restoreRequest);
            } catch (Exception e) {
                log.error("Failed to restore inventory for product {}", item.getProductId(), e);
                // Vẫn tiếp tục hủy đơn nhưng log warning
            }
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.CANCELLED);
        return orderRepository.save(order);
    }

    private OrderItem convertToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setSize(cartItem.getSize());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getPrice());
        orderItem.setDiscountedPrice(cartItem.getDiscountedPrice());
        return orderItem;
    }

    @Transactional
    public Order updateOrderPaymentAndStatus(Long orderId, PaymentStatus paymentStatus, PaymentMethod paymentMethod) {
        Order order = findOrderById(orderId);
        if(order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new AppException(
                    "Cannot update payment for order with status: " + order.getPaymentStatus(),
                    HttpStatus.BAD_REQUEST
            );
        }

        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(paymentStatus);

        // Nếu thanh toán thành công -> confirm đơn hàng
        if (paymentStatus == PaymentStatus.COMPLETED) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }

        return orderRepository.save(order);
    }


    public Page<Order> searchOrdersForAdmin(String search, String status,
                                            String paymentStatus, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        Specification<Order> spec = Specification.where(null);

        // ✅ SAFE SEARCH - Kiểm tra trước khi parse
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                try {
                    Long searchId = Long.parseLong(search.trim());
                    return cb.or(
                            cb.equal(root.get("id"), searchId),
                            cb.equal(root.get("userId"), searchId)
                    );
                } catch (NumberFormatException e) {
                    // Nếu không phải số, search theo user email (cần join)
                    return cb.like(
                            cb.lower(root.get("userId").as(String.class)),
                            "%" + search.toLowerCase() + "%"
                    );
                }
            });
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("orderStatus"), OrderStatus.valueOf(status))
            );
        }

        if (paymentStatus != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("paymentStatus"), PaymentStatus.valueOf(paymentStatus))
            );
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"),
                            startDate.atStartOfDay())
            );
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"),
                            endDate.atTime(23, 59, 59))
            );
        }

        return orderRepository.findAll(spec, pageable);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = findOrderById(orderId);

        // ✅ Validate transition hợp lệ
        validateStatusTransition(order.getOrderStatus(), newStatus);

        // ✅ Xử lý logic đặc biệt khi DELIVERED
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.COMPLETED);

            // Tăng quantity_sold cho từng product
            for (OrderItem item : order.getOrderItems()) {
                productServiceClient.increaseQuantitySold(new InventoryCheckRequest(item.getProductId(), item.getQuantity()));
            }
        }

        order.setOrderStatus(newStatus);
        return orderRepository.save(order);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // PENDING → CONFIRMED → SHIPPED → DELIVERED
        // CANCELLED có thể từ PENDING hoặc CONFIRMED

        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new AppException("Cannot update completed order", HttpStatus.BAD_REQUEST);
        }

        if (current == OrderStatus.SHIPPED || next == OrderStatus.CONFIRMED) {
            throw new AppException("Cannot update this status order", HttpStatus.BAD_REQUEST);
        }

        if (current == OrderStatus.CONFIRMED || next == OrderStatus.PENDING) {
            throw new AppException("Cannot update this status order", HttpStatus.BAD_REQUEST);
        }

        if (current == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED) {
            throw new AppException("Cannot update this status order", HttpStatus.BAD_REQUEST);
        }
    }

    public RevenueChartDTO calculateRevenueChart(LocalDate start, LocalDate end) {
        LocalDateTime startTime = start != null
                ? start.atStartOfDay()
                : LocalDate.now().minusMonths(6).atStartOfDay();

        LocalDateTime endTime = end != null
                ? end.atTime(23, 59, 59)
                : LocalDateTime.now();

        // Query orders grouped by date
        List<Object[]> results = orderRepository.findRevenueGroupedByDate(
                startTime, endTime, OrderStatus.DELIVERED
        );

        List<RevenueChartDTO.RevenueDataPoint> dataPoints = results.stream()
                .map(row -> new RevenueChartDTO.RevenueDataPoint(
                        row[0].toString(), // date
                        ((BigDecimal) row[1]).doubleValue() // revenue
                ))
                .collect(Collectors.toList());

        RevenueChartDTO dto = new RevenueChartDTO();
        dto.setDataPoints(dataPoints);
        dto.setTotalRevenue(dataPoints.stream()
                .mapToDouble(RevenueChartDTO.RevenueDataPoint::getRevenue)
                .sum());

        return dto;
    }

    public OrderStatsDTO calculateOrderStats(LocalDate startDate, LocalDate endDate) {
        OrderStatsDTO stats = new OrderStatsDTO();

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;

        stats.setTotalOrders(orderRepository.countByDateRange(start, end));
        stats.setPendingOrders(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.PENDING, start, end));
        stats.setConfirmedOrders(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CONFIRMED, start, end));
        stats.setShippedOrders(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.SHIPPED, start, end));
        stats.setDeliveredOrders(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.DELIVERED, start, end));
        stats.setCancelledOrders(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CANCELLED, start, end));

        Double totalRevenue = orderRepository.sumRevenueByDateRange(start, end);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);

        // Revenue this month
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Double monthRevenue = orderRepository.sumRevenueByDateRange(monthStart, null);
        stats.setRevenueThisMonth(monthRevenue != null ? monthRevenue : 0.0);

        // Average order value
        stats.setAverageOrderValue(stats.getTotalOrders() > 0
                ? stats.getTotalRevenue() / stats.getTotalOrders()
                : 0.0);

        return stats;
    }
}


