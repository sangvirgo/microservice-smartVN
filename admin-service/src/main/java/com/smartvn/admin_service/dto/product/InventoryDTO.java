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