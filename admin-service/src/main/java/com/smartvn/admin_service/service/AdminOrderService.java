package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.OrderServiceClient;
import com.smartvn.admin_service.dto.order.OrderAdminViewDTO;
import com.smartvn.admin_service.dto.order.OrderStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
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
public class AdminOrderService {
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

    public OrderAdminViewDTO updateStatus(Long orderId, String status) {
        ResponseEntity<ApiResponse<OrderAdminViewDTO>> response =
                orderServiceClient.updateOrderStatus(orderId, status);
        return handleResponse(response, "Failed to update order status");
    }

    public OrderStatsDTO getStats(LocalDate startDate, LocalDate endDate) {
        ResponseEntity<ApiResponse<OrderStatsDTO>> response =
                orderServiceClient.getOrderStats(startDate, endDate);
        return handleResponse(response, "Failed to get stats");
    }

    /**
     * Hàm tiện ích xử lý response từ Feign Client.
     *
     * @param response     ResponseEntity từ Feign client
     * @param errorMessage Thông báo lỗi nếu request thất bại
     * @return Dữ liệu từ response nếu thành công
     * @throws ResponseStatusException Nếu request thất bại hoặc không có dữ liệu
     */
    private <T> T handleResponse(ResponseEntity<ApiResponse<T>> response, String errorMessage) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getData() != null) {
            return response.getBody().getData();
        } else {
            HttpStatus status = (HttpStatus) response.getStatusCode();
            String message = (response.getBody() != null && response.getBody().getMessage() != null)
                    ? response.getBody().getMessage()
                    : errorMessage;
            log.error("{} - Status: {}, Message: {}", errorMessage, status, message);
            // Ném lỗi để GlobalExceptionHandler có thể bắt và trả về response chuẩn
            throw new ResponseStatusException(status, message);
        }
    }

    // Overload cho trường hợp response không có data (Void)
    private void handleVoidResponse(ResponseEntity<ApiResponse<Void>> response, String errorMessage) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            HttpStatus status = (HttpStatus) response.getStatusCode(); // Cast an toàn hơn
            String message = (response.getBody() != null && response.getBody().getMessage() != null)
                    ? response.getBody().getMessage()
                    : errorMessage;
            log.error("{} - Status: {}, Message: {}", errorMessage, status, message);
            throw new ResponseStatusException(status, message);
        }
        // Thành công thì không cần làm gì
    }
}