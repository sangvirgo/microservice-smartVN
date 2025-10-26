package com.smartvn.product_service.dto.admin;

import com.smartvn.product_service.dto.InventoryDTO;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailForEditDTO {
    private Long id;
    private String title;
    private String brand;
    private String description;

    // Specifications
    private String color;
    private String weight;
    private String dimension;
    private String batteryType;
    private String batteryCapacity;
    private String ramCapacity;
    private String romCapacity;
    private String screenSize;
    private String connectionPort;
    private String detailedReview;
    private String powerfulPerformance;

    // Category
    private Long categoryId;
    private String categoryName;

    // Status
    private boolean isActive;
    private Long quantitySold;
    private Double averageRating;
    private Integer numRatings;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relations
    private List<InventoryDTO> inventories;
    private List<ImageDTO> images;
}