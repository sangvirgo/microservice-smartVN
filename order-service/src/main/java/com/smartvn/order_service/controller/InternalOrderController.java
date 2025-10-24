package com.smartvn.order_service.controller;


import com.smartvn.order_service.dto.admin.OrderAdminViewDTO;
import com.smartvn.order_service.dto.admin.OrderStatsDTO;
import com.smartvn.order_service.dto.response.ApiResponse;
import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.model.Order;
import com.smartvn.order_service.repository.OrderRepository;
import com.smartvn.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/internal/orders")
public class InternalOrderController {
    private final OrderRepository  orderRepository;
    private final OrderService  orderService;

    @GetMapping("/users/{userId}/products/{productId}/purchased")
    public ResponseEntity<Boolean> hasUserPurchasedProduct(@PathVariable Long userId, @PathVariable Long productId) {
        boolean hasPurchased = orderRepository.existsByUserIdAndProductIdAndDelivered(userId, productId);
        return  ResponseEntity.ok(hasPurchased);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<Page<OrderAdminViewDTO>>> getAllOrdersAdmin(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderService.searchOrdersForAdmin(search, status, paymentStatus, startDate, endDate, pageable);
        Page<OrderAdminViewDTO> dtos = orders.map(this::convertToAdminDTO);

        return ResponseEntity.ok(ApiResponse.success(dtos, "Orders retrieved"));
    }

    private OrderAdminViewDTO convertToAdminDTO(Order order) {
        OrderAdminViewDTO dto = new OrderAdminViewDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setUserEmail();
    }

    /**
     * ✅ CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderAdminViewDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam("status") String newStatus) {

        Order updated = orderService.updateOrderStatus(orderId, OrderStatus.valueOf(newStatus));
        OrderAdminViewDTO dto = convertToAdminDTO(updated);

        return ResponseEntity.ok(ApiResponse.success(dto, "Order status updated"));
    }

    /**
     * ✅ LẤY THỐNG KÊ ĐƠN HÀNG
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<OrderStatsDTO>> getOrderStats(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        OrderStatsDTO stats = orderService.calculateOrderStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats, "Stats retrieved"));
    }
}
