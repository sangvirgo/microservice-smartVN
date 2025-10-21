package com.smartvn.product_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * ✅ SINGLE STORE VERSION - Không còn store_id
 * Mỗi product có nhiều variants (size) với giá & số lượng riêng
 */
@Entity
@Table(name = "inventory",
        indexes = {
                @Index(name = "idx_inventory_product", columnList = "product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_size", columnNames = {"product_id", "size"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    /**
     * Size/Variant của sản phẩm
     * VD: "128GB", "256GB", "512GB" cho điện thoại
     *     "S", "M", "L" cho quần áo
     */
    @Column(length = 50)
    private String size;

    /**
     * Số lượng tồn kho
     */
    @Column(nullable = false)
    private Integer quantity = 0;

    /**
     * Giá gốc
     */
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    /**
     * Phần trăm giảm giá (0-100)
     */
    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent = 0;

    /**
     * Giá sau khi giảm (tự động tính)
     */
    @Column(name = "discounted_price", precision = 19, scale = 2)
    private BigDecimal discountedPrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Tự động tính giá sau khi giảm
     */
    @PrePersist
    @PreUpdate
    public void calculateDiscountedPrice() {
        if (price != null && discountPercent != null && discountPercent > 0) {
            BigDecimal discount = price.multiply(BigDecimal.valueOf(discountPercent / 100.0));
            this.discountedPrice = price.subtract(discount).setScale(0, RoundingMode.HALF_UP);
        } else if (price != null) {
            this.discountedPrice = price; // ✅ THÊM dòng này
        }
    }

    /**
     * Kiểm tra còn hàng
     */
    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }
}