package com.smartvn.admin_service.client;

import com.smartvn.admin_service.dto.order.OrderAdminViewDTO;
import com.smartvn.admin_service.dto.order.OrderStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

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
    public ResponseEntity<ApiResponse<OrderAdminViewDTO>> updateOrderStatus(Long orderId, String newStatus) {
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
}
