package com.smartvn.admin_service.client;

import com.smartvn.admin_service.config.FeignClientConfig;
import com.smartvn.admin_service.dto.dashboard.RevenueChartDTO;
import com.smartvn.admin_service.dto.order.OrderAdminViewDTO; // Cần tạo DTO này
import com.smartvn.admin_service.dto.order.OrderStatsDTO; // Cần tạo DTO này
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.enums.OrderStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate; // Hoặc LocalDateTime tùy endpoint gốc

/**
 * Feign Client để giao tiếp với Order Service.
 */
@FeignClient(name = "order-service", configuration = FeignClientConfig.class, fallback = OrderServiceFallback.class)
public interface OrderServiceClient {

    /**
     * Lấy danh sách đơn hàng cho admin với bộ lọc và phân trang.
     * Endpoint này cần được tạo trong Order Service.
     * @param page Số trang
     * @param size Kích thước trang
     * @param search Từ khóa tìm kiếm (order ID, user ID,...)
     * @param status Lọc theo trạng thái đơn hàng
     * @param paymentStatus Lọc theo trạng thái thanh toán
     * @param startDate Lọc từ ngày
     * @param endDate Lọc đến ngày
     * @return Trang kết quả OrderAdminViewDTO
     */
    @GetMapping("${api.prefix}/internal/orders/admin/all")
    ResponseEntity<ApiResponse<Page<OrderAdminViewDTO>>> getAllOrdersAdmin(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status, // Dùng String cho linh hoạt
            @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    /**
     * Lấy chi tiết một đơn hàng cho admin.
     * Có thể dùng endpoint public hoặc tạo endpoint internal riêng.
     * @param orderId ID đơn hàng
     * @return OrderAdminViewDTO
     */
    @GetMapping("${api.prefix}/internal/orders/admin/{orderId}") // Hoặc dùng endpoint public nếu đủ thông tin
    ResponseEntity<ApiResponse<OrderAdminViewDTO>> getOrderDetailAdmin(@PathVariable("orderId") Long orderId);

    /**
     * Cập nhật trạng thái của một đơn hàng.
     * Endpoint này cần được tạo trong Order Service.
     * @param orderId ID đơn hàng
     * @param newStatus Trạng thái mới (CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
     * @return OrderAdminViewDTO đã được cập nhật
     */
    @PutMapping("${api.prefix}/internal/orders/{orderId}/status")
    ResponseEntity<ApiResponse<OrderAdminViewDTO>> updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam("status") OrderStatus newStatus);

    /**
     * Lấy thống kê về đơn hàng và doanh thu.
     * Endpoint này cần được tạo trong Order Service.
     * @param startDate Lọc từ ngày
     * @param endDate Lọc đến ngày
     * @return OrderStatsDTO chứa thông tin thống kê
     */
    @GetMapping("${api.prefix}/internal/orders/stats")
    ResponseEntity<ApiResponse<OrderStatsDTO>> getOrderStats(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);


    @GetMapping("${api.prefix}/internal/orders/revenue-chart")
    ResponseEntity<ApiResponse<RevenueChartDTO>> getRevenueChart(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    );
}