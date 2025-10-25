package com.smartvn.product_service.dto.admin;

import lombok.Data;

@Data
public class ProductStatsDTO {
    private Long totalProducts;
    private Long activeProducts;
}