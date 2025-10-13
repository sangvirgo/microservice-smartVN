package com.smartvn.product_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BulkProductRequest {
    private List<ProductItemDTO> products;

    @Data
    public static class ProductItemDTO {
        // === THÔNG TIN CƠ BẢN (Product entity) ===
        private String title;
        private String brand;
        private String color;
        private String weight;
        private String dimension;
        private String batteryType;
        private String batteryCapacity;
        private String ramCapacity;
        private String romCapacity;
        private String screenSize;
        private String detailedReview;
        private String powerfulPerformance;
        private String connectionPort;
        private String description;

        // === CATEGORY ===
        private String topLevelCategory;    // Tên category level 1
        private String secondLevelCategory; // Tên category level 2

        // === IMAGES ===
        private List<ImageUrlDTO> imageUrls;

        private List<InventoryItemDTO> variants; // Các phiên bản size khác nhau
    }

    @Data
    public static class ImageUrlDTO {
        private String fileName;
        private String fileType;
        private String downloadUrl;
    }

    @Data
    public static class InventoryItemDTO {
        private String size;              // "256GB", "512GB", "1TB"
        private Integer quantity;         // Số lượng tồn kho
        private BigDecimal price;         // Giá gốc
        private Integer discountPercent;  // % giảm giá (0-100)
    }
}