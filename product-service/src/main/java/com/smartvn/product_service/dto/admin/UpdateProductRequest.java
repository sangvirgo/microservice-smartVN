package com.smartvn.product_service.dto.admin;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProductRequest {

    @Size(max = 100)
    private String title;

    @Size(max = 50)
    private String brand;

    @Size(max = 500)
    private String description;

    // Specifications
    @Size(max = 50)
    private String color;

    @Size(max = 50)
    private String weight;

    @Size(max = 100)
    private String dimension;

    @Size(max = 50)
    private String batteryType;

    @Size(max = 50)
    private String batteryCapacity;

    @Size(max = 50)
    private String ramCapacity;

    @Size(max = 50)
    private String romCapacity;

    @Size(max = 50)
    private String screenSize;

    @Size(max = 100)
    private String connectionPort;

    private String detailedReview;
    private String powerfulPerformance;
}