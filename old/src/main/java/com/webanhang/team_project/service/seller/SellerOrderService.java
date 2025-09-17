package com.webanhang.team_project.service.seller;

import com.webanhang.team_project.enums.OrderStatus;
import com.webanhang.team_project.enums.PaymentStatus;
import com.webanhang.team_project.model.Order;
import com.webanhang.team_project.model.Product;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.repository.OrderRepository;
import com.webanhang.team_project.repository.ProductRepository;
import com.webanhang.team_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerOrderService implements ISellerOrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public Page<Order> getSellerOrders(Long sellerId, int page, int size, String search,
                                       OrderStatus status, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, size);

        userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        // Convert LocalDate to LocalDateTime if needed
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        // Use repository method with all filters applied at database level
        return orderRepository.findSellerOrdersWithFilters(
                sellerId, search, status, startDateTime, endDateTime, pageable);
    }
    @Override
    public Order getOrderDetail(Long sellerId, Long orderId) {
        userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Đơn hàng không thuộc người bán này");
        }

        return order;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long sellerId, Long orderId, OrderStatus status) {
        Order order = getOrderDetail(sellerId, orderId);

        order.setOrderStatus(status);

        if (status == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        } else if (status == OrderStatus.CANCELLED) {
            order.setPaymentStatus(PaymentStatus.CANCELLED);
        }

        return orderRepository.save(order);
    }

    @Override
    public Map<String, Object> getOrderStatistics(Long sellerId, String period) {
        Map<String, Object> statistics = new HashMap<>();

        userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        if ("day".equals(period)) {
            startDate = LocalDate.now().atStartOfDay();
        } else if ("week".equals(period)) {
            startDate = LocalDate.now().minusDays(7).atStartOfDay();
        } else if ("month".equals(period)) {
            startDate = LocalDate.now().minusMonths(1).atStartOfDay();
        } else if ("year".equals(period)) {
            startDate = LocalDate.now().minusYears(1).atStartOfDay();
        } else {
            startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        }

        List<Order> sellerOrders = orderRepository.findBySellerIdAndOrderDateBetween(sellerId, startDate, endDate);

        long pendingCount = sellerOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.PENDING)
                .count();

        long confirmedCount = sellerOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.CONFIRMED)
                .count();

        long shippedCount = sellerOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.SHIPPED)
                .count();

        long deliveredCount = sellerOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .count();

        long cancelledCount = sellerOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.CANCELLED)
                .count();

        BigDecimal totalRevenue = sellerOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .map(order -> BigDecimal.valueOf(order.getTotalDiscountedPrice() != null ?
                        order.getTotalDiscountedPrice() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        statistics.put("totalOrders", sellerOrders.size());
        statistics.put("pendingOrders", pendingCount);
        statistics.put("confirmedOrders", confirmedCount);
        statistics.put("shippedOrders", shippedCount);
        statistics.put("deliveredOrders", deliveredCount);
        statistics.put("cancelledOrders", cancelledCount);
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("period", period);

        return statistics;
    }
}