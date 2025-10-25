package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.dto.product.ReviewDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.exceptions.BaseAdminService;
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
public class AdminReviewService extends BaseAdminService {
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
}