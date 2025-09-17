package com.webanhang.team_project.service.order;

import com.webanhang.team_project.dto.order.OrderDTO;
import com.webanhang.team_project.dto.order.OrderDetailDTO;
import com.webanhang.team_project.enums.OrderStatus;
import com.webanhang.team_project.enums.PaymentMethod;
import com.webanhang.team_project.enums.PaymentStatus;
import com.webanhang.team_project.model.*;
import com.webanhang.team_project.repository.AddressRepository;
import com.webanhang.team_project.repository.CartRepository;
import com.webanhang.team_project.repository.OrderRepository;
import com.webanhang.team_project.repository.ProductRepository;
import com.webanhang.team_project.service.cart.ICartService;
import com.webanhang.team_project.service.product.ProductService;
import com.webanhang.team_project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ICartService cartService;
    private final ModelMapper modelMapper;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final UserService userService;

    @Override
    public OrderDTO convertToDto(Order order) {
        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found with ID: {}", orderId);
                    return new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
                });
    }

    @Override
    public List<Order> userOrderHistory(Long userId, OrderStatus status) {
        if (status != null) {
            return orderRepository.findByUserIdAndOrderStatus(userId, status);
        } else {
            return orderRepository.findByUserId(userId);
        }
    }

    @Override
    @Transactional
    public List<Order> placeOrder(Long addressId, User user) {
        if (user == null) {
            log.error("User object is null when placing order.");
            throw new IllegalArgumentException("Thông tin người dùng không hợp lệ.");
        }
        if (addressId == null) {
            log.error("Address ID is null when placing order for user: {}", user.getEmail());
            throw new IllegalArgumentException("Địa chỉ giao hàng không được để trống.");
        }

        Cart cart = cartRepository.findByUserId(user.getId());
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            log.warn("Attempted to place order with an empty cart for user: {}", user.getEmail());
            throw new RuntimeException("Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm vào giỏ hàng trước khi đặt hàng.");
        }

        Address shippingAddress = user.getAddress().stream()
                .filter(a -> Objects.equals(a.getId(), addressId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Address not found with ID: {} for user: {}", addressId, user.getEmail());
                    return new RuntimeException("Địa chỉ giao hàng không hợp lệ hoặc không thuộc về bạn.");
                });

        // Lọc và gom nhóm các CartItem có Product và Product.sellerId hợp lệ
        Map<Long, List<CartItem>> itemsBySeller = cart.getCartItems().stream()
                .filter(item -> {
                    if (item.getProduct() == null) {
                        log.warn("CartItem ID {} has null Product. This item will be skipped.", item.getId());
                        return false;
                    }
                    if (item.getProduct().getSellerId() == null) {
                        log.warn("Product ID {} (Title: '{}') in CartItem ID {} has null sellerId. This item will be skipped.",
                                item.getProduct().getId(), item.getProduct().getTitle(), item.getId());
                        return false;
                    }
                    return true; // Chỉ giữ lại những item có product và sellerId hợp lệ
                })
                .collect(Collectors.groupingBy(item -> item.getProduct().getSellerId()));

        List<Order> createdOrders = new ArrayList<>();

        if (itemsBySeller.isEmpty()) {
            // Nếu không có sản phẩm nào hợp lệ (tất cả đều bị lọc ra do thiếu product hoặc sellerId)
            log.warn("No cart items with valid product and sellerId found for user: {}. Original cart size: {}. No orders will be created.",
                    user.getEmail(), cart.getCartItems().size());
            // Không ném lỗi ở đây, sẽ trả về danh sách createdOrders rỗng.
            // Controller sẽ kiểm tra và thông báo cho người dùng.
        } else {
            // Tạo đơn hàng cho từng người bán
            for (Map.Entry<Long, List<CartItem>> entry : itemsBySeller.entrySet()) {
                Long sellerId = entry.getKey(); // Đã được đảm bảo không null do filter ở trên
                List<CartItem> sellerItems = entry.getValue();

                // Tính toán tổng tiền cho các sản phẩm của người bán này
                int sellerOriginalPrice = 0;
                int sellerDiscountedPrice = 0;
                int sellerTotalItems = 0;

                for (CartItem item : sellerItems) {
                    sellerOriginalPrice += item.getPrice() * item.getQuantity();
                    sellerDiscountedPrice += item.getDiscountedPrice() * item.getQuantity();
                    sellerTotalItems += item.getQuantity();
                }
                int sellerDiscount = sellerOriginalPrice - sellerDiscountedPrice;

                // Tạo Order
                Order order = new Order();
                order.setUser(user);
                order.setSellerId(sellerId);
                order.setOrderDate(LocalDateTime.now());
                order.setShippingAddress(shippingAddress);
                order.setOrderStatus(OrderStatus.PENDING);
                order.setPaymentStatus(PaymentStatus.PENDING);
                order.setPaymentMethod(PaymentMethod.COD);

                order.setOriginalPrice(sellerOriginalPrice);
                order.setTotalItems(sellerTotalItems);
                order.setDiscount(sellerDiscount);
                order.setTotalDiscountedPrice(sellerDiscountedPrice);

                Order savedOrderIntermediate = orderRepository.save(order);
                log.info("Saved intermediate order ID: {} for seller ID: {}", savedOrderIntermediate.getId(), sellerId);

                List<OrderItem> orderItems = new ArrayList<>();
                for (CartItem cartItem : sellerItems) {
                    Product product = cartItem.getProduct();
                    String sizeName = cartItem.getSize();
                    int orderedQuantity = cartItem.getQuantity();

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(savedOrderIntermediate);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(orderedQuantity);
                    orderItem.setPrice(cartItem.getPrice());
                    orderItem.setSize(sizeName);
                    orderItem.setDiscountPercent(cartItem.getDiscountPercent());
                    orderItem.setDiscountedPrice(cartItem.getDiscountedPrice());
                    orderItem.setDeliveryDate(LocalDateTime.now().plusDays(7));
                    orderItems.add(orderItem);

                    if (product.getSizes() == null || product.getSizes().isEmpty()) {
                        log.error("Product ID {} (Title: '{}') has no sizes defined, but CartItem ID {} specifies size '{}'.",
                                product.getId(), product.getTitle(), cartItem.getId(), sizeName);
                        throw new RuntimeException("Lỗi cấu hình sản phẩm: Sản phẩm '" + product.getTitle() + "' không có thông tin kích thước.");
                    }

                    ProductSize targetSize = product.getSizes().stream()
                            .filter(ps -> ps.getName().equals(sizeName))
                            .findFirst()
                            .orElseThrow(() -> {
                                log.error("Size '{}' not found for product ID {} (Title: '{}') in cart item ID {}.",
                                        sizeName, product.getId(), product.getTitle(), cartItem.getId());
                                return new RuntimeException("Lỗi đặt hàng: Size '" + sizeName +
                                        "' không tồn tại cho sản phẩm '" + product.getTitle() + "'. Vui lòng kiểm tra lại giỏ hàng.");
                            });

                    if (targetSize.getQuantity() == null || targetSize.getQuantity() < orderedQuantity) {
                        log.warn("Insufficient stock for Product ID {} (Title: '{}'), Size: '{}'. Requested: {}, Available: {}",
                                product.getId(), product.getTitle(), sizeName, orderedQuantity, targetSize.getQuantity());
                        throw new RuntimeException("Số lượng sản phẩm '" + product.getTitle() + "' (Size: " + sizeName +
                                ") không đủ trong kho. Hiện còn: " + (targetSize.getQuantity() != null ? targetSize.getQuantity() : 0) + ". Vui lòng giảm số lượng hoặc chọn sản phẩm khác.");
                    }
                    targetSize.setQuantity(targetSize.getQuantity() - orderedQuantity);

                    Long currentQuantitySold = product.getQuantitySold() != null ? product.getQuantitySold() : 0L;
                    product.setQuantitySold(currentQuantitySold + orderedQuantity);
                    productRepository.save(product); // Lưu thay đổi của product (bao gồm cả ProductSize)
                    log.info("Updated stock for Product ID {}: Size {} quantity now {}, total sold now {}.",
                            product.getId(), sizeName, targetSize.getQuantity(), product.getQuantitySold());
                }

                savedOrderIntermediate.setOrderItems(orderItems);
                Order finalSavedOrder = orderRepository.save(savedOrderIntermediate);
                createdOrders.add(finalSavedOrder);
                log.info("Successfully created and saved final order ID: {} for seller ID: {}", finalSavedOrder.getId(), sellerId);
            }
        }

        // Xóa giỏ hàng CHỈ KHI có ít nhất một đơn hàng được tạo thành công
        if (!createdOrders.isEmpty()) {
            cartService.clearCart(user.getId());
            log.info("Cart cleared for user ID: {} as {} order(s) were created.", user.getId(), createdOrders.size());
        } else {
            if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                log.warn("No orders were created for user ID: {}. Cart was not cleared because all items might have been invalid (e.g., missing sellerId).", user.getId());
            }
        }

        return createdOrders;
    }

    @Override
    @Transactional
    public Order confirmedOrder(Long orderId) {
        Order order = findOrderById(orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Đơn hàng không thể xác nhận ở trạng thái hiện tại (" + order.getOrderStatus() + ")");
        }
        order.setOrderStatus(OrderStatus.CONFIRMED);
        log.info("Order ID {} confirmed.", orderId);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order shippedOrder(Long orderId) {
        Order order = findOrderById(orderId);
        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Đơn hàng phải được xác nhận trước khi gửi (trạng thái hiện tại: " + order.getOrderStatus() + ")");
        }
        order.setOrderStatus(OrderStatus.SHIPPED);
        log.info("Order ID {} shipped.", orderId);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order deliveredOrder(Long orderId) {
        Order order = findOrderById(orderId);
        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Đơn hàng phải được gửi trước khi giao (trạng thái hiện tại: " + order.getOrderStatus() + ")");
        }
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDeliveryDate(LocalDateTime.now());
        log.info("Order ID {} delivered.", orderId);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);
        if (order.getOrderStatus() == OrderStatus.DELIVERED || order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái " + order.getOrderStatus());
        }

        if (order.getOrderStatus() == OrderStatus.PENDING || order.getOrderStatus() == OrderStatus.CONFIRMED) {
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                String sizeName = orderItem.getSize();
                int cancelledQuantity = orderItem.getQuantity();

                ProductSize targetSize = product.getSizes().stream()
                        .filter(ps -> ps.getName().equals(sizeName))
                        .findFirst()
                        .orElseThrow(() -> {
                            log.error("Size '{}' not found for product ID {} (Title: '{}') during cancellation of order item ID {}.",
                                    sizeName, product.getId(), product.getTitle(), orderItem.getId());
                            return new RuntimeException("Lỗi hủy đơn hàng: Không tìm thấy size '" + sizeName +
                                    "' cho sản phẩm '" + product.getTitle() + "'.");
                        });

                targetSize.setQuantity(targetSize.getQuantity() + cancelledQuantity);

                Long currentQuantitySold = product.getQuantitySold() != null ? product.getQuantitySold() : 0L;
                product.setQuantitySold(Math.max(0, currentQuantitySold - cancelledQuantity));

                productRepository.save(product);
                log.info("Restored stock for Product ID {}: Size {} quantity now {}, total sold now {}.",
                        product.getId(), sizeName, targetSize.getQuantity(), product.getQuantitySold());
            }
        } else {
            log.warn("Order ID {} is in status {} and stock was not restored upon cancellation (or restoration logic needs review for this state).", orderId, order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        if (order.getPaymentMethod() == PaymentMethod.VNPAY && order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            log.info("Order ID {} cancelled. Payment status set to REFUNDED for VNPAY.", orderId);
        } else {
            order.setPaymentStatus(PaymentStatus.CANCELLED);
            log.info("Order ID {} cancelled. Payment status set to CANCELLED.", orderId);
        }

        return orderRepository.save(order);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            log.info("No orders found in the system.");
        }
        return orders;
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = findOrderById(orderId);
        orderRepository.delete(order);
        log.info("Order ID {} deleted.", orderId);
    }


    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStatistics(LocalDate start, LocalDate end) {
        // Convert LocalDate to LocalDateTime
        LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = end != null ? end.atTime(23, 59, 59) : null;

        // Use optimized count queries instead of loading all data
        long totalOrders = orderRepository.countOrdersByStatusAndDateRange(null, startDateTime, endDateTime);
        long pendingOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.PENDING, startDateTime, endDateTime);
        long confirmedOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CONFIRMED, startDateTime, endDateTime);
        long deliveredOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.DELIVERED, startDateTime, endDateTime);
        long cancelledOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CANCELLED, startDateTime, endDateTime);

        // Get total revenue efficiently
        Double totalRevenue = orderRepository.sumRevenueByDateRange(startDateTime, endDateTime);
        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;

        double averageOrderValue = deliveredOrders > 0 ? totalRevenue / deliveredOrders : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", totalOrders);
        result.put("pendingOrders", pendingOrders);
        result.put("confirmedOrders", confirmedOrders);
        result.put("completedOrders", deliveredOrders);
        result.put("cancelledOrders", cancelledOrders);
        result.put("totalRevenue", totalRevenue);
        result.put("averageOrderValue", averageOrderValue);

        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailDTO> getAllOrdersByJF() {
        List<Order> orders = orderRepository.findAllWithUserOrderByOrderDateDesc();
        return orders.stream()
                .map(OrderDetailDTO::new)
                .collect(Collectors.toList());
    }


    @Override
    public Page<OrderDetailDTO> getAllOrdersWithFilters(String search, OrderStatus status,
                                                        LocalDate startDate, LocalDate endDate,
                                                        Pageable pageable) {
        // Convert LocalDate to LocalDateTime
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        // Reuse the existing query but remove sellerId filter
        Page<Order> orders = orderRepository.findAdminOrdersWithFilters(
                search, status, startDateTime, endDateTime, pageable);

        return orders.map(OrderDetailDTO::new);
    }

}