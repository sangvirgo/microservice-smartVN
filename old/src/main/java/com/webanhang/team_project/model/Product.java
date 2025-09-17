package com.webanhang.team_project.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String color;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    @Column(name = "title")
    private String title;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must be less than 50 characters")
    @Column(name = "brand")
    private String brand;

    @Column(name = "weight")
    private String weight;

    @Column(name = "dimension")
    private String dimension;

    @Column(name = "battery_type")
    private String batteryType;

    @Column(name = "battery_capacity")
    private String batteryCapacity;

    @Column(name = "ram_capacity")
    private String ramCapacity;

    @Column(name = "rom_capacity")
    private String romCapacity;

    @Column(name = "screen_size")
    private String screenSize;

    @Column(name = "detailed_review")
    private String detailedReview;

    @Column(name = "powerful_performance")
    private String powerfulPerformance;

    @Column(name = "connection_port")
    private String connectionPort;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    @Column(precision = 19, scale = 2)
    private int price;

    @Formula("(SELECT COALESCE(SUM(s.quantity), 0) FROM sizes s WHERE s.product_id = id)")
    private int quantity; // Vẫn giữ trường này để lấy giá trị tính toán

    @Size(max = 500, message = "Description must be less than 500 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Min(value = 0, message = "Discount percent must be greater than or equal to 0")
    @Max(value = 100, message = "Discount percent must be less than or equal to 100")
    @Column(name = "discount_persent")
    private int discountPersent;

    @Min(value = 0, message = "Discounted price must be greater than or equal to 0")
    @Column(name = "discounted_price")
    private int discountedPrice;

    @Size(max = 255, message = "Image URL must be less than 255 characters")
    @Column(name = "image_urls")
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Quan trọng: FetchType.LAZY
    private List<ProductSize> sizes = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @JoinColumn(name = "seller_id")
    private Long sellerId;

    // so luong danh gia
    @Column(name = "num_ratings", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int numRatings = 0; // Đổi tên và đặt giá trị mặc định

    @Column(name = "average_rating", nullable = false, columnDefinition = "DOUBLE DEFAULT 0.0")
    private double averageRating = 0.0; // Đặt giá trị mặc định

//
    @Column(name = "quantity_sold")
    private Long quantitySold;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void updateDiscountedPrice() {
        this.discountedPrice = price - (price * discountPersent / 100);
    }
}
