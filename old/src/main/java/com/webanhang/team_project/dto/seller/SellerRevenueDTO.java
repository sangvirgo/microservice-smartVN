package com.webanhang.team_project.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerRevenueDTO {
    private Long sellerId;
    private String sellerName;
    private BigDecimal totalRevenue;
    private int totalOrders;
    private double growth; // Tỷ lệ tăng trưởng

    // Constructor không có growth (để tương thích với code cũ)
    public SellerRevenueDTO(Long sellerId, String sellerName, BigDecimal totalRevenue, int totalOrders) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.growth = 0.0; // Giá trị mặc định
    }
}