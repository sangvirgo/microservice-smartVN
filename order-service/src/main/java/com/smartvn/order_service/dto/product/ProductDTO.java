package com.smartvn.order_service.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho Product từ Product Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String title;
    private String brand;
    private String description;

    // Giá - lấy từ inventory đầu tiên hoặc giá trung bình
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private Integer discountPercent;

    // Thông tin khác
    private String color;
    private String weight;
    private Boolean isActive;

    // Images
    private List<String> images; // Chỉ lưu URL

    // Inventory info
    private Boolean hasStock;
    private Integer totalStock;

    /**
     * Helper: Lấy ảnh đầu tiên
     */
    public String getFirstImageUrl() {
        return (images != null && !images.isEmpty()) ? images.get(0) : null;
    }
}