package com.smartvn.order_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items",
        indexes = {
                @Index(name = "idx_cart", columnList = "cart_id"),
                @Index(name = "idx_product", columnList = "product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_product_size",
                        columnNames = {"cart_id", "product_id", "size"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "discounted_price", precision = 19, scale = 2)
    private BigDecimal discountedPrice;

    @Column(length = 50)
    private String size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    // Không tạo quan hệ với Product, chỉ lưu productId
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper method
    @Transient
    private Integer discountPercent; // Tính từ price và discountedPrice khi cần

    public Integer getDiscountPercent() {
        if (price != null && discountedPrice != null
                && price.compareTo(BigDecimal.ZERO) > 0
                && discountedPrice.compareTo(price) < 0) { // ✅ Kiểm tra discountedPrice < price

            BigDecimal discount = price.subtract(discountedPrice);
            return discount.multiply(BigDecimal.valueOf(100))
                    .divide(price, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
        }
        return 0;
    }
}