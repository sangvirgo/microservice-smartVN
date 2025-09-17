package com.webanhang.team_project.controller.admin;



import com.webanhang.team_project.dto.order.OrderDTO;
import com.webanhang.team_project.dto.order.OrderDetailDTO;
import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.enums.OrderStatus;
import com.webanhang.team_project.model.Order;
import com.webanhang.team_project.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin/orders")
public class AdminOrderController {

    private final IOrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDetailDTO> ordersPage = orderService.getAllOrdersWithFilters(
                search, status,
                startDate != null ? LocalDate.parse(startDate) : null,
                endDate != null ? LocalDate.parse(endDate) : null,
                pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("orders", ordersPage.getContent());
        response.put("pagination", Map.of(
                "currentPage", ordersPage.getNumber(),
                "pageSize", ordersPage.getSize(),
                "totalElements", ordersPage.getTotalElements(),
                "totalPages", ordersPage.getTotalPages(),
                "hasNext", ordersPage.hasNext(),
                "hasPrevious", ordersPage.hasPrevious()
        ));

        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách đơn hàng thành công"));
    }

    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse> confirmOrder(@PathVariable Long orderId) {
        Order order = orderService.confirmedOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xác nhận đơn hàng thành công"));
    }

    @PutMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse> shipOrder(@PathVariable Long orderId) {
        Order order = orderService.shippedOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Chuyển trạng thái vận chuyển thành công"));
    }

    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponse> deliverOrder(@PathVariable Long orderId) {
        Order order = orderService.deliveredOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đánh dấu đã giao hàng thành công"));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable Long orderId) {
        Order order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Hủy đơn hàng thành công"));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa đơn hàng thành công"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getOrderStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        Map<String, Object> stats = orderService.getOrderStatistics(start, end);
        return ResponseEntity.ok(ApiResponse.success(stats, "Get order statistics success"));
    }
}
