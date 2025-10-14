package com.smartvn.order_service.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request để check/reduce/restore inventory
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckRequest {
    private Long productId;
    private String size;
    private Integer quantity;

    /**
     * Constructor cho trường hợp không có size
     */
    public InventoryCheckRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}