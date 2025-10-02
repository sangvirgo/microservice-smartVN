package com.smartvn.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho DANH SÁCH sản phẩm (Product Listing Page)
 * Chỉ chứa thông tin cần thiết để hiển thị nhanh
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductListingDTO {

    private Long id;
    private String title;
    private String brand;

    // Hình ảnh chính (thumbnail)
    private String thumbnailUrl;

    // ============================================
    // GIÁ - Hiển thị theo RANGE
    // ============================================

    /**
     * Giá gốc: "25.000.000đ - 35.000.000đ"
     * Hoặc: "25.000.000đ" (nếu chỉ có 1 giá)
     */
    private String priceRange;

    /**
     * Giá sau giảm (nếu có)
     */
    private String discountedPriceRange;

    /**
     * Raw values để client có thể sort/filter
     */
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minDiscountedPrice;
    private BigDecimal maxDiscountedPrice;

    /**
     * Badge hiển thị: "Giảm 20%", "Sale 30%"
     */
    private Integer maxDiscountPercent;
    private Boolean hasDiscount;

    // ============================================
    // TÌNH TRẠNG
    // ============================================

    /**
     * Còn hàng: true/false
     * Để hiển thị "Hết hàng" hoặc "Còn hàng"
     */
    private Boolean inStock;

    /**
     * Có nhiều size/phiên bản
     * Để hiển thị: "Có 3 phiên bản"
     */
    private Integer variantCount;

    // ============================================
    // THỐNG KÊ
    // ============================================

    private Double averageRating;
    private Integer numRatings;
    private Long quantitySold;

    /**
     * Badge: "Bán chạy", "Mới"
     */
    private List<String> badges;
}