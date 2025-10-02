package com.smartvn.product_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_category", columnList = "category_id"),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_title", columnList = "title"),
        @Index(name = "idx_price_range", columnList = "min_price, max_price")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 50, nullable = false)
    private String brand;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String weight;

    @Column(length = 50)
    private String dimension;

    @Column(name = "battery_type", length = 50)
    private String batteryType;

    @Column(name = "battery_capacity", length = 50)
    private String batteryCapacity;

    @Column(name = "ram_capacity", length = 50)
    private String ramCapacity;

    @Column(name = "rom_capacity", length = 50)
    private String romCapacity;

    @Column(name = "screen_size", length = 50)
    private String screenSize;

    @Column(name = "detailed_review", columnDefinition = "TEXT")
    private String detailedReview;

    @Column(name = "powerful_performance", columnDefinition = "TEXT")
    private String powerfulPerformance;

    @Column(name = "connection_port", length = 100)
    private String connectionPort;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ============================================
    // GIẢI PHÁP: HIỂN THỊ KHOẢNG GIÁ (PRICE RANGE)
    // ============================================

    /**
     * Giá THẤP NHẤT trong tất cả các size/store
     * Ví dụ: "Từ 24.500.000đ"
     * Được sync từ Inventory Service
     */
    @Column(name = "min_price", precision = 19, scale = 2)
    private BigDecimal minPrice;

    /**
     * Giá CAO NHẤT trong tất cả các size/store
     * Dùng để hiển thị range: "24.500.000đ - 35.000.000đ"
     */
    @Column(name = "max_price", precision = 19, scale = 2)
    private BigDecimal maxPrice;

    /**
     * Giá sau giảm giá THẤP NHẤT
     * Dùng để highlight deal tốt nhất
     */
    @Column(name = "min_discounted_price", precision = 19, scale = 2)
    private BigDecimal minDiscountedPrice;

    /**
     * Giá sau giảm giá CAO NHẤT
     */
    @Column(name = "max_discounted_price", precision = 19, scale = 2)
    private BigDecimal maxDiscountedPrice;

    /**
     * Flag để biết có đang giảm giá không
     * Dùng để hiển thị badge "SALE"
     */
    @Column(name = "has_discount", nullable = false)
    private Boolean hasDiscount = false;

    /**
     * Phần trăm giảm giá tối đa
     * Ví dụ: "Giảm đến 20%"
     */
    @Column(name = "max_discount_percent")
    private Integer maxDiscountPercent = 0;

    // ============================================
    // TỒN KHO TỔNG (Để hiển thị "Còn hàng / Hết hàng")
    // ============================================

    /**
     * Tổng số lượng tồn kho trong TẤT CẢ các store/size
     * Chỉ dùng để check "Còn hàng" hay không
     * KHÔNG dùng cho business logic
     */
    @Column(name = "total_stock", nullable = false)
    private Integer totalStock = 0;

    // ============================================
    // THỐNG KÊ
    // ============================================

    @Column(name = "num_ratings", nullable = false)
    private Integer numRatings = 0;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating = 0.0;

    @Column(name = "quantity_sold", nullable = false)
    private Long quantitySold = 0L;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "warning_count", nullable = false)
    private Integer warningCount = 0;

    // ============================================
    // QUAN HỆ
    // ============================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ============================================
    // TRANSIENT FIELDS
    // ============================================

    /**
     * Chi tiết đầy đủ về giá theo size/store
     * Load từ Inventory Service khi xem chi tiết sản phẩm
     */
    @Transient
    private List<InventoryInfo> inventoryDetails;

    /**
     * Helper method để hiển thị giá trên UI
     */
    @Transient
    public String getPriceDisplay() {
        if (minPrice == null) return "Liên hệ";

        if (hasDiscount && minDiscountedPrice != null) {
            if (minDiscountedPrice.compareTo(maxDiscountedPrice) == 0) {
                return String.format("%,.0fđ", minDiscountedPrice);
            }
            return String.format("%,.0fđ - %,.0fđ", minDiscountedPrice, maxDiscountedPrice);
        }

        if (minPrice.compareTo(maxPrice) == 0) {
            return String.format("%,.0fđ", minPrice);
        }
        return String.format("%,.0fđ - %,.0fđ", minPrice, maxPrice);
    }

    // ============================================
    // INNER CLASS
    // ============================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryInfo {
        private Long inventoryId;
        private Long storeId;
        private String storeName;
        private String size;
        private Integer quantity;
        private BigDecimal price;
        private Integer discountPercent;
        private BigDecimal discountedPrice;
        private Boolean inStock;
    }
}