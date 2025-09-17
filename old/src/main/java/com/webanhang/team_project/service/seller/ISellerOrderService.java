package com.webanhang.team_project.service.seller;

import com.webanhang.team_project.enums.OrderStatus;
import com.webanhang.team_project.model.Order;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.Map;

public interface ISellerOrderService {
    Page<Order> getSellerOrders(Long sellerId, int page, int size, String search,
                                OrderStatus status, LocalDate startDate, LocalDate endDate);
    Order getOrderDetail(Long sellerId, Long orderId);
    Order updateOrderStatus(Long sellerId, Long orderId, OrderStatus status);
    Map<String, Object> getOrderStatistics(Long sellerId, String period);
}
