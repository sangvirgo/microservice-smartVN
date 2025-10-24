package com.smartvn.admin_service.dto.product;

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
}