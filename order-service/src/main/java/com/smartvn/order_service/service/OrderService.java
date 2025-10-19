package com.smartvn.order_service.service;

import com.smartvn.order_service.client.ProductServiceClient;
import com.smartvn.order_service.client.UserServiceClient;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
}


