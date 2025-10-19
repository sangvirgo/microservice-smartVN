package com.smartvn.order_service.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDTO {
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private Integer discountPercent;
    private BigDecimal discountedPrice; // Tự động tính
}