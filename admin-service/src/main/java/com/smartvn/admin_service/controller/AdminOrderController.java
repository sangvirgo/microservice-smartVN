package com.smartvn.admin_service.controller;

import com.smartvn.admin_service.dto.order.OrderAdminViewDTO;
import com.smartvn.admin_service.dto.order.OrderStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.enums.OrderStatus;
import com.smartvn.admin_service.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        Page<OrderAdminViewDTO> orders = adminOrderService.getAllOrders(page, size, search, status, paymentStatus, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrder(@PathVariable("orderId") Long orderId) {
        OrderAdminViewDTO order = adminOrderService.getOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order retrieved"));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> updateStatusOrder(@PathVariable("orderId") Long orderId, @RequestBody Map<String, String> request) {

        String status = request.get("status");

        if(status ==null || status.trim().isEmpty() ) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("New status is required"));
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            OrderAdminViewDTO order = adminOrderService.updateStatus(orderId, orderStatus);
            return ResponseEntity.ok(ApiResponse.success(order, "Changed order status"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status. Allowed values: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED"));
        }

    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getStats(LocalDate startDate, LocalDate endDate) {
        OrderStatsDTO stats = adminOrderService.getStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats, "Order stats retrieved"));
    }


}
