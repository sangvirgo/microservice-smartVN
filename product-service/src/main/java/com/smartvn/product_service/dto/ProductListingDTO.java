package com.smartvn.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho DANH SÁCH sản phẩm (Product Listing Page)
 * Dữ liệu được tính toán real-time từ bảng Inventory.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductListingDTO {

    private Long id;
    private String title;
    private String brand;
    private String thumbnailUrl;

    // GIÁ - Tính toán động từ Inventory
    private String priceRange;
    private String discountedPriceRange;
    private boolean hasDiscount;

    // TÌNH TRẠNG
    private boolean inStock;
    private Integer variantCount; // Số phiên bản (size) khác nhau

    // THỐNG KÊ
    private Double averageRating;
    private Integer numRatings;
    private Long quantitySold;
    private List<String> badges;
}