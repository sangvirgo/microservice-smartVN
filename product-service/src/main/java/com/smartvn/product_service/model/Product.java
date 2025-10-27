package com.smartvn.product_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_category", columnList = "category_id"),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_title", columnList = "title")
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

    @Column(length = 100)
    private String dimension;

    @Column(name = "battery_type", length = 50)
    private String batteryType;

    @Column(name = "battery_capacity", length = 50)
    private String batteryCapacity;

    @Column(name = "ram_capacity", length = 50)
    private String ramCapacity;

    @Column(name = "rom_capacity", length = 50)
    private String romCapacity;

    @Column(name = "screen_size", length = 200)
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Inventory> inventories = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Kiểm tra sản phẩm có còn hàng không (bất kỳ size nào)
     */
    public boolean hasStock() {
        return inventories.stream().anyMatch(inv -> inv.getQuantity() > 0);
    }

    /**
     * Lấy số lượng variants
     */
    public int getVariantCount() {
        return inventories != null ? inventories.size() : 0;
    }
}