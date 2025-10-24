package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.dto.product.ReviewDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReviewService {
    private final ProductServiceClient productServiceClient;

    public Page<ReviewDTO> getAllReviews(int page, int size, String status,
                                         Long productId, Long userId) {
        ResponseEntity<ApiResponse<Page<ReviewDTO>>> response =
                productServiceClient.getAllReviewsAdmin(page, size, status, productId, userId);
        return handleResponse(response, "Failed to get reviews");
    }

    public void deleteReview(Long reviewId) {
        ResponseEntity<ApiResponse<Void>> response =
                productServiceClient.deleteReview(reviewId);
        handleVoidResponse(response, "Failed to delete review");
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