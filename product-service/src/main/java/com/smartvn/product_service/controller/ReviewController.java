package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.ReviewDTO;
import com.smartvn.product_service.dto.ReviewRequest;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Review;
import com.smartvn.product_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * API để lấy danh sách đánh giá của một sản phẩm (phân trang).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getProductReviews(
            @PathVariable Long productId,
            Pageable pageable) {

        Page<ReviewDTO> reviews = reviewService.getProductReviews(productId, pageable);

        ApiResponse<Page<ReviewDTO>> response = ApiResponse.<Page<ReviewDTO>>builder()
                .message("Reviews fetched successfully for product " + productId)
                .data(reviews)
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
            @RequestBody @Valid ReviewRequest reviewRequest,  // ✅ Thêm @Valid để validate
            @RequestHeader(value = "X-User-Id", required = true) Long userId) {  // ✅ Đổi tên biến

        Review newReview = reviewService.createReview(userId, productId, reviewRequest);

        ApiResponse<Review> response = ApiResponse.<Review>builder()
                .message("Review created successfully.")
                .data(newReview)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}