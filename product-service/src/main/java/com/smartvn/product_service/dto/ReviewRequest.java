package com.smartvn.product_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {
    private Long productId;

    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @NotNull(message = "Rating is required")
    private Integer rating;

    @Size(max = 500, message = "Content must be less than 500 characters")
    private String reviewContent;
}
