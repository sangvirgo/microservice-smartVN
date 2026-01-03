package com.smartvn.admin_service.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductExportDTO {
    private String product_id;
    private String name;
    private String description;
    private String brand;
    private String category;
}
