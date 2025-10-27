package com.smartvn.admin_service.dto.product;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String userEmail; // Lấy từ UserService
    private String userName;
    private Long productId;
    private String productTitle; // Lấy từ Product
    private Integer rating;
    private String reviewContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}