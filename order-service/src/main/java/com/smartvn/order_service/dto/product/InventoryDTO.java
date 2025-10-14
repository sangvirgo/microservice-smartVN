package com.smartvn.order_service.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho Inventory từ Product Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private Long id;
    private Long productId;
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private Integer discountPercent;

    /**
     * Kiểm tra còn hàng
     */
    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }

    /**
     * Kiểm tra đủ số lượng
     */
    public boolean hasEnoughStock(int requestedQuantity) {
        return quantity != null && quantity >= requestedQuantity;
    }
}