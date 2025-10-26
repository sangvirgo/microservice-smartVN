package com.smartvn.product_service.dto.admin;

import com.smartvn.product_service.dto.InventoryDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductAdminViewDTO {
    private Long id;
    private String title;
    private String brand;
    private String categoryName;
    private boolean isActive;
    private Long quantitySold;
    private Double averageRating;
    private Integer numRatings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InventoryDTO> inventories; // Danh sách các variants
    private String imageUrl;
}
