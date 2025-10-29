package com.smartvn.order_service.model;

import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.enums.PaymentMethod;
import com.smartvn.order_service.enums.PaymentStatus;
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
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_user", columnList = "user_id"),
                @Index(name = "idx_order_status", columnList = "order_status"),
                @Index(name = "idx_payment_status", columnList = "payment_status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 50, nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50, nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Không tạo quan hệ với User, chỉ lưu userId
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    // Không tạo quan hệ với Address, chỉ lưu addressId
    @Column(name = "shipping_address_id", nullable = false)
    private Long shippingAddressId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private PaymentDetail paymentDetail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Các trường tính toán (transient hoặc có thể lưu vào DB)
    @Transient
    private Integer originalPrice;

    @Transient
    private Integer discount;

    @Transient
    private Integer totalDiscountedPrice;

    // Helper methods
    public void calculateTotals() {
        // 1. Tổng số items
        this.totalItems = orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        // 2. Giá gốc (chưa giảm)
        this.originalPrice = orderItems.stream()
                .mapToInt(item -> item.getPrice().intValue() * item.getQuantity())
                .sum();

        // 3. Giá sau giảm
        this.totalDiscountedPrice = orderItems.stream()
                .mapToInt(item -> {
                    BigDecimal itemPrice = (item.getDiscountedPrice() != null && item.getDiscountedPrice().compareTo(BigDecimal.ZERO) > 0)
                            ? item.getDiscountedPrice()
                            : item.getPrice();
                    return itemPrice.intValue() * item.getQuantity();
                })
                .sum();

        // 4. Tính discount
        this.discount = originalPrice - totalDiscountedPrice;

        // 5. totalPrice = giá sau giảm (dùng cho thanh toán)
        this.totalPrice = BigDecimal.valueOf(totalDiscountedPrice);
    }
}