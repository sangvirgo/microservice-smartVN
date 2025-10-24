package com.smartvn.admin_service.dto.order;

import lombok.Data;

@Data
public class OverviewStatsDTO {
    private Long totalUsers;
    private Long newUsersThisMonth;
    private Long totalOrders;
    private Long pendingOrders;
    private Double totalRevenue;
    private Double revenueThisMonth;
    private Long totalProducts;
    private Long activeProducts;
}
