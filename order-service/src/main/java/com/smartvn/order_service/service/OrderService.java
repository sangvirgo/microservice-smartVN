package com.smartvn.order_service.service;

import com.smartvn.order_service.client.ProductServiceClient;
import com.smartvn.order_service.client.UserServiceClient;
import com.smartvn.order_service.dto.product.InventoryCheckRequest;
import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.exceptions.AppException;
import com.smartvn.order_service.model.Order;
import com.smartvn.order_service.model.OrderItem;
import com.smartvn.order_service.repository.CartItemRepository;
import com.smartvn.order_service.repository.CartRepository;
import com.smartvn.order_service.repository.OrderItemRepository;
import com.smartvn.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository  cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public List<Order> placeOrder(Long addressId, Long userId) {
        Boolean isValidAddress = userServiceClient.validateUserAddress(addressId, userId);
        if (!isValidAddress) {
            throw new AppException(
                    "Address does not belong to user or is invalid",
                    HttpStatus.BAD_REQUEST
            );
        }

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
    public Order cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);

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


    
}
