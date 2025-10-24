package com.smartvn.order_service.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemAdminDTO {
    private Long id;
    private Long productId;
    private String productTitle;
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discountedPrice;
}