package com.smartvn.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho CHI TIẾT sản phẩm (Product Detail Page)
 * ✅ SINGLE STORE VERSION - Không còn thông tin store
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {

    private Long id;
    private String title;
    private String brand;
    private String description;
    private String detailedReview;
    private String powerfulPerformance;

    // Specs
    private String color;
    private String weight;
    private String dimension;
    private String batteryType;
    private String batteryCapacity;
    private String ramCapacity;
    private String romCapacity;
    private String screenSize;
    private String connectionPort;

    // Images
    private List<String> imageUrls;

    // Category
    private Long categoryId;
    private String categoryName;

    /**
     * Danh sách các phiên bản (size) với giá khác nhau
     */
    private List<PriceVariantDTO> priceVariants;

    private Double averageRating;
    private Integer numRatings;
    private List<ReviewSummaryDTO> recentReviews;

    private Long quantitySold;
    private Boolean isActive;

    /**
     * Thông tin giá của MỘT phiên bản (size)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceVariantDTO {
        private Long inventoryId;
        private String size;                  // "128GB", "256GB", etc.
        private BigDecimal price;             // Giá gốc
        private Integer discountPercent;      // % giảm giá
        private BigDecimal discountedPrice;   // Giá sau giảm
        private Integer quantity;             // Số lượng tồn
        private Boolean inStock;              // Còn hàng hay không
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewSummaryDTO {
        private Long id;
        private Long userId;
        private String userName;
        private String userAvatar;
        private Integer rating;
        private String reviewContent;
        private String createdAt;
    }

    public ProductDTO toSimpleDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(this.id);
        dto.setTitle(this.title);
        dto.setBrand(this.brand);
        dto.setDescription(this.description);
        dto.setColor(this.color);
        dto.setWeight(this.weight);
        dto.setIsActive(this.isActive);
        dto.setImages(this.imageUrls);

        // Lấy giá từ variant đầu tiên (hoặc rẻ nhất)
        if (priceVariants != null && !priceVariants.isEmpty()) {
            PriceVariantDTO firstVariant = priceVariants.get(0);
            dto.setPrice(firstVariant.getPrice());
            dto.setDiscountedPrice(firstVariant.getDiscountedPrice());
            dto.setDiscountPercent(firstVariant.getDiscountPercent());

            // Tổng stock
            dto.setTotalStock(priceVariants.stream()
                    .mapToInt(PriceVariantDTO::getQuantity)
                    .sum());
            dto.setHasStock(dto.getTotalStock() > 0);
        }

        return dto;
    }
}