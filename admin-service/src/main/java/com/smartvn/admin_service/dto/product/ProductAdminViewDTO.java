package com.smartvn.admin_service.dto.product;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductAdminViewDTO {
    private Long id;
    private String title;
    private String brand;
    private String categoryName;
    private Long categoryId;
    private boolean isActive;
    private int warningCount;
    private Long quantitySold;
    private Double averageRating;
    private Integer numRatings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InventoryDTO> inventories; // Danh sách các variants
    // Thêm các trường khác nếu cần
}