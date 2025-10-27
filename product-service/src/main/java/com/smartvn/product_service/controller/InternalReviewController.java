package com.smartvn.product_service.controller;

import com.smartvn.product_service.client.UserServiceClient;
import com.smartvn.product_service.dto.UserInfoDTO;
import com.smartvn.product_service.dto.admin.ReviewAdminDTO;
import com.smartvn.product_service.dto.admin.UserDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.exceptions.AppException;
import com.smartvn.product_service.model.Review;
import com.smartvn.product_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${api.prefix}/internal/reviews/admin")
public class InternalReviewController {
    private final ReviewService reviewService;
    private final UserServiceClient userServiceClient;

    /**
     * ✅ XÓA REVIEW
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.ok(
                ApiResponse.<Page<ReviewAdminDTO>>builder()
                        .data(null)  // Giữ nguyên Page object
                        .message("Reviews deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    /**
     * ✅ LẤY TẤT CẢ REVIEWS CHO ADMIN
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllReviewsAdmin(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "userId", required = false) Long userId) {

        if (page < 0 || size < 1 || size > 100) {
            throw new AppException("Invalid pagination", HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Review> reviews = reviewService.searchReviewsForAdmin(
                status, productId, userId, pageable);

        Page<ReviewAdminDTO> dtos = reviews.map(this::convertToAdminDTO);

        // ✅ DÙNG CÁI NÀY:
        return ResponseEntity.ok(
                ApiResponse.<Page<ReviewAdminDTO>>builder()
                        .data(dtos)  // Giữ nguyên Page object
                        .message("Reviews retrieved")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    private ReviewAdminDTO convertToAdminDTO(Review review) {
        ReviewAdminDTO dto = new ReviewAdminDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setProductId(review.getProduct().getId());
        dto.setProductTitle(review.getProduct().getTitle());
        dto.setRating(review.getRating());
        dto.setReviewContent(review.getReviewContent());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        // ✅ FIX: Gọi getUserById() thay vì getUserInfo()
        try {
            UserDTO userInfo = userServiceClient.getUserById(review.getUserId());
            dto.setUserEmail(userInfo.getEmail());
            dto.setUserName(userInfo.getFirstName() + " " + userInfo.getLastName());
        } catch (Exception e) {
            log.warn("Failed to fetch user info for review {}", review.getId());
            dto.setUserEmail("Unknown");
            dto.setUserName("Unknown User");
        }

        return dto;
    }
}
