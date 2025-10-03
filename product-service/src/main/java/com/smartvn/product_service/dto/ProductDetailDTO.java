package com.smartvn.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho CHI TIẾT sản phẩm (Product Detail Page)
 * Chứa đầy đủ thông tin bao gồm giá theo từng size/store
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

    // ============================================
    // GIÁ THEO SIZE/STORE - Chi tiết đầy đủ
    // ============================================

    /**
     * Danh sách giá theo từng phiên bản (size) và cửa hàng
     * Được load từ Inventory Service
     */
    private List<PriceVariantDTO> priceVariants;

    /**
     * Cửa hàng được đề xuất (gần nhất với user)
     */
    private Long recommendedStoreId;
    private String recommendedStoreName;

    // ============================================
    // REVIEWS
    // ============================================

    private Double averageRating;
    private Integer numRatings;
    private List<ReviewSummaryDTO> recentReviews;

    // ============================================
    // THỐNG KÊ
    // ============================================

    private Long quantitySold;
    private Boolean isActive;

    // ============================================
    // INNER CLASSES
    // ============================================

    /**
     * Thông tin giá của MỘT phiên bản (size) tại MỘT cửa hàng
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceVariantDTO {
        private Long inventoryId;
        private Long storeId;
        private String storeName;
        private String size;
        private BigDecimal price;
        private Integer discountPercent;
        private BigDecimal discountedPrice;
        private Integer quantity;
        private Boolean inStock;
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
}