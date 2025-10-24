package com.smartvn.admin_service.dto.order;

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