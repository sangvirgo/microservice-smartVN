package com.smartvn.order_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items",
        indexes = {
                @Index(name = "idx_order", columnList = "order_id"),
                @Index(name = "idx_product", columnList = "product_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

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
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    // Không tạo quan hệ với Product, chỉ lưu productId
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Helper method để tính discount percent
    @Transient
    public Integer getDiscountPercent() {
        if (price != null && discountedPrice != null
                && price.compareTo(BigDecimal.ZERO) > 0
                && discountedPrice.compareTo(price) < 0) {

            BigDecimal discount = price.subtract(discountedPrice);
            return discount.multiply(BigDecimal.valueOf(100))
                    .divide(price, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
        }
        return 0;
    }
}