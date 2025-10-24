package com.smartvn.admin_service.controller;

import com.smartvn.admin_service.dto.product.ReviewDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {
    private final AdminReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long userId
    ) {
        Page<ReviewDTO> reviewDTOS = reviewService.getAllReviews(page, size, status, productId, userId);
        return ResponseEntity.ok(ApiResponse.success(reviewDTOS, "Reviews retrieved successfully"));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }
}
