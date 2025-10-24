package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.admin.ReviewAdminDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Review;
import com.smartvn.product_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${api.prefix}/internal/admin/review")
public class InternalReviewController {
    private final ReviewService reviewService;

    /**
     * ✅ XÓA REVIEW
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted"));
    }

    /**
     * ✅ LẤY TẤT CẢ REVIEWS CHO ADMIN
     */
    @GetMapping("/reviews/admin/all")
    public ResponseEntity<ApiResponse<Page<ReviewAdminDTO>>> getAllReviewsAdmin(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "userId", required = false) Long userId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewService.searchReviewsForAdmin(status, productId, userId, pageable);
        Page<ReviewAdminDTO> dtos = reviews.map(this::convertToAdminDTO);

        return ResponseEntity.ok(ApiResponse.success(dtos, "Reviews retrieved"));
    }
}
