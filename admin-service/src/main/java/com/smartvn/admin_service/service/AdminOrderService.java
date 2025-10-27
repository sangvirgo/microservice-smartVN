package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.OrderServiceClient;
import com.smartvn.admin_service.dto.order.OrderAdminViewDTO;
import com.smartvn.admin_service.dto.order.OrderStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.enums.OrderStatus;
import com.smartvn.admin_service.exceptions.BaseAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderService extends BaseAdminService {
    private final OrderServiceClient orderServiceClient;

    public Page<OrderAdminViewDTO> getAllOrders(int page, int size, String search,
                                                String status, String paymentStatus,
                                                LocalDate startDate, LocalDate endDate) {
        ResponseEntity<ApiResponse<Page<OrderAdminViewDTO>>> response =
                orderServiceClient.getAllOrdersAdmin(page, size, search, status,
                        paymentStatus, startDate, endDate);
        return handleResponse(response, "Failed to get orders");
    }

    public OrderAdminViewDTO getOrderDetail(Long orderId) {
        ResponseEntity<ApiResponse<OrderAdminViewDTO>> response =
                orderServiceClient.getOrderDetailAdmin(orderId);
        return handleResponse(response, "Failed to get order detail");
    }

    public OrderAdminViewDTO updateStatus(Long orderId, OrderStatus status) {
        ResponseEntity<ApiResponse<OrderAdminViewDTO>> response =
                orderServiceClient.updateOrderStatus(orderId, status);
        return handleResponse(response, "Failed to update order status");
    }

    public OrderStatsDTO getStats(LocalDate startDate, LocalDate endDate) {
        ResponseEntity<ApiResponse<OrderStatsDTO>> response =
                orderServiceClient.getOrderStats(startDate, endDate);
        return handleResponse(response, "Failed to get stats");
    }

}