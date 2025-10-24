package com.smartvn.admin_service.client;

import com.smartvn.admin_service.dto.order.OrderAdminViewDTO;
import com.smartvn.admin_service.dto.order.OrderStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public class OrderServiceFallback implements OrderServiceClient{
    @Override
    public ResponseEntity<ApiResponse<Page<OrderAdminViewDTO>>> getAllOrdersAdmin(int page, int size, String search, String status, String paymentStatus, LocalDate startDate, LocalDate endDate) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<OrderAdminViewDTO>> getOrderDetailAdmin(Long orderId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<OrderAdminViewDTO>> updateOrderStatus(Long orderId, String newStatus) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<OrderStatsDTO>> getOrderStats(LocalDate startDate, LocalDate endDate) {
        return null;
    }
}
