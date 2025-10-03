package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.ReviewRequest;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Review;
import com.smartvn.product_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * API để lấy danh sách đánh giá của một sản phẩm (phân trang).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Review>>> getProductReviews(
            @PathVariable Long productId,
            Pageable pageable) {

        Page<Review> reviews = reviewService.getProductReviews(productId, pageable);

        ApiResponse<Page<Review>> response = ApiResponse.<Page<Review>>builder()
                .message("Reviews fetched successfully for product " + productId)
                .result(reviews)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * API để người dùng tạo một đánh giá mới.
     * Endpoint này cần được bảo vệ (chỉ user đã đăng nhập mới được gọi).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(
            @PathVariable Long productId,
            @RequestBody ReviewRequest reviewRequest,
            @RequestHeader("X-User-Id") Long userId) { // Giả sử User ID được truyền qua header từ API Gateway

        Review newReview = reviewService.createReview(userId, productId, reviewRequest);

        ApiResponse<Review> response = ApiResponse.<Review>builder()
                .message("Review created successfully.")
                .result(newReview)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}