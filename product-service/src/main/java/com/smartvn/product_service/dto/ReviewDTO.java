package com.smartvn.product_service.dto;

import com.smartvn.product_service.model.Review;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private String reviewContent;
    private Long productId;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userAvatar; // Thêm field này
    private LocalDateTime createdAt;
    private Integer rating;

    public ReviewDTO(Review review) {
        this.id = review.getId();
        this.reviewContent = review.getReviewContent(); // ✅ FIX: Đổi từ getContent() -> getReviewContent()
        this.productId = review.getProduct().getId();
        this.userId = review.getUserId();
        this.createdAt = review.getCreatedAt();
        this.rating = review.getRating();
    }
}