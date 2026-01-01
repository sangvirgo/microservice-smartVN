package com.smartvn.product_service.dto.ai;

import lombok.Data;

@Data
public class ProductExportDTO {
    private String product_id;  // String để AI xử lý
    private String name;
    private String description;
    private String brand;
    private String category;
}