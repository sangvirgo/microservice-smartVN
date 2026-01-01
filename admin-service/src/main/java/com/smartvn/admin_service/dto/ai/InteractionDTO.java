package com.smartvn.admin_service.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionDTO {
    private Long userId;
    private Long productId;
    private String interactionType; // CLICK, VIEW, ADD_TO_CART, PURCHASE
    private LocalDateTime timestamp;
    private Integer quantity; // Nullable, chỉ dùng cho PURCHASE
    private Double rating; // Nullable, chỉ dùng cho REVIEW
}