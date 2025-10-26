package com.smartvn.admin_service.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100)
    private String title;

    @NotBlank(message = "Brand is required")
    @Size(max = 50)
    private String brand;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    // Specifications
    @Size(max = 50) private String color;
    @Size(max = 50) private String weight;
    @Size(max = 100) private String dimension;
    @Size(max = 50) private String batteryType;
    @Size(max = 50) private String batteryCapacity;
    @Size(max = 50) private String ramCapacity;
    @Size(max = 50) private String romCapacity;
    @Size(max = 50) private String screenSize;
    @Size(max = 100) private String connectionPort;
    private String detailedReview;
    private String powerfulPerformance;

    // Variants (REQUIRED)
    @NotEmpty(message = "Product must have at least one variant")
    @Valid
    private List<CreateInventoryDTO> variants;

    // Image URLs (Optional)
    @Valid
    private List<ImageUrlDTO> imageUrls;

    @Data
    public static class CreateInventoryDTO {
        @NotBlank @Size(max = 50)
        private String size;

        @NotNull @Min(0)
        private Integer quantity;

        @NotNull @DecimalMin("0.0")
        private BigDecimal price;

        @Min(0) @Max(100)
        private Integer discountPercent = 0;
    }

    @Data
    public static class ImageUrlDTO {
        @NotBlank
        private String downloadUrl;
        private String fileName;
        private String fileType;
    }
}
