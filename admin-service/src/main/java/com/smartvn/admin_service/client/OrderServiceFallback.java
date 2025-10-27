package com.smartvn.admin_service.client;

import com.smartvn.admin_service.dto.dashboard.RevenueChartDTO;
import com.smartvn.admin_service.dto.order.OrderAdminViewDTO;
import com.smartvn.admin_service.dto.order.OrderStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;

@Slf4j
public class OrderServiceFallback implements OrderServiceClient{
    @Override
    public ResponseEntity<ApiResponse<Page<OrderAdminViewDTO>>> getAllOrdersAdmin(int page, int size, String search, String status, String paymentStatus, LocalDate startDate, LocalDate endDate) {
        log.error("Order Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Order service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<OrderAdminViewDTO>> getOrderDetailAdmin(Long orderId) {
        log.error("Order Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Order service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<OrderAdminViewDTO>> updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.error("Order Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Order service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<OrderStatsDTO>> getOrderStats(LocalDate startDate, LocalDate endDate) {
        log.error("Order Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Order service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<RevenueChartDTO>> getRevenueChart(
            LocalDate startDate, LocalDate endDate) {
        log.error("Order Service unavailable. Cannot fetch revenue chart.");

        // Trả về empty chart thay vì null
        RevenueChartDTO emptyChart = new RevenueChartDTO();
        emptyChart.setDataPoints(Collections.emptyList());
        emptyChart.setTotalRevenue(0.0);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.success(
                        emptyChart,
                        "Service unavailable - showing empty data"
                ));
    }
}
