package com.smartvn.product_service.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {

    // === BASIC INFO ===
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must be less than 50 characters")
    private String brand;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    private Long categoryId;

    private String topLevelCategory;
    private String secondLevelCategory;

    // === SPECIFICATIONS ===
    @Size(max = 50)
    private String color;

    @Size(max = 50)
    private String weight;

    @Size(max = 100)
    private String dimension;

    @Size(max = 50)
    private String batteryType;

    @Size(max = 50)
    private String batteryCapacity;

    @Size(max = 50)
    private String ramCapacity;

    @Size(max = 50)
    private String romCapacity;

    @Size(max = 50)
    private String screenSize;

    @Size(max = 100)
    private String connectionPort;

    private String detailedReview;
    private String powerfulPerformance;

    // === VARIANTS (REQUIRED) ===
    @NotEmpty(message = "Product must have at least one variant")
    @Valid
    private List<CreateInventoryDTO> variants;

    // === IMAGE URLS (Optional) ===
    @Valid
    private List<ImageUrlDTO> imageUrls;

    @Data
    public static class CreateInventoryDTO {
        @NotBlank(message = "Size is required")
        @Size(max = 50)
        private String size;

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be >= 0")
        private Integer quantity;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be > 0")
        private BigDecimal price;

        @Min(value = 0, message = "Discount must be >= 0")
        @Max(value = 100, message = "Discount must be <= 100")
        private Integer discountPercent = 0;
    }

    @Data
    public static class ImageUrlDTO {
        @NotBlank(message = "Image URL is required")
        private String downloadUrl;

        private String fileName;
        private String fileType;
    }

    public boolean hasCategoryInfo() {
        return categoryId != null ||
                (topLevelCategory != null && secondLevelCategory != null);
    }
}