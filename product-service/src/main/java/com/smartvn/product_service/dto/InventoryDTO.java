package com.smartvn.product_service.dto;

import com.smartvn.product_service.model.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO {
    private Long id;
    private Long productId;
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private Integer discountPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryDTO(Inventory inv) {
        this.id = inv.getId();
        this.productId = inv.getProduct().getId();
        this.size = inv.getSize();
        this.quantity = inv.getQuantity();
        this.price = inv.getPrice();
        this.discountPercent = inv.getDiscountPercent();
        this.discountedPrice = inv.getDiscountedPrice();
        this.createdAt = inv.getCreatedAt();
        this.updatedAt = inv.getUpdatedAt();
    }
}
