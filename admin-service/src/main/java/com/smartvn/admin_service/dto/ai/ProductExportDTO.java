package com.smartvn.admin_service.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đơn giản để export product cho AI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductExportDTO {
    // === REQUIRED FIELDS ===
    private String product_id;
    private String name;
    private String description;
    private String brand;
    private String category;

    // === OPTIONAL FIELDS (nhưng nên có) ===
    private String top_level_category;
    private Double price;
    private Double average_rating;
    private Long quantity_sold;
}
