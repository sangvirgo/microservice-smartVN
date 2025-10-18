package com.smartvn.order_service.service;

import com.smartvn.order_service.client.ProductServiceClient;
import com.smartvn.order_service.client.UserServiceClient;
import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.model.Order;
import com.smartvn.order_service.repository.CartItemRepository;
import com.smartvn.order_service.repository.CartRepository;
import com.smartvn.order_service.repository.OrderItemRepository;
import com.smartvn.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    }

    @Transactional
    public void cancelOrder(Long  orderId) {

    }
}
