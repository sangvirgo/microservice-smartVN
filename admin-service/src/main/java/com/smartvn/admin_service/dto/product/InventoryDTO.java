package com.smartvn.admin_service.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryDTO {
    private Long id;
    private Long productId; // Thêm productId để dễ tham chiếu
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private Integer discountPercent;
    private BigDecimal discountedPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}