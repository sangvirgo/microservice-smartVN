package com.webanhang.team_project.dto.product;


import com.webanhang.team_project.model.Category;
import com.webanhang.team_project.model.Image;
import com.webanhang.team_project.model.ProductSize;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateProductRequest {
    private String title;

    private String description;

    private Integer price;

    private Integer discountPersent;

    private Integer quantity;

    private String brand;

    private String color;

    // Add specification fields
    private String weight;
    private String dimension;
    private String batteryType;
    private String batteryCapacity;
    private String ramCapacity;
    private String romCapacity;
    private String screenSize;
    private String detailedReview;
    private String powerfulPerformance;
    private String connectionPort;

    // Add category fields like CreateProductRequest
    private String topLevelCategory;
    private String secondLevelCategory;

    private List<ProductSize> sizes = new ArrayList<>();
    private List<Image> imageUrls = new ArrayList<>();
}
