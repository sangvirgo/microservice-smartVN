package com.smartvn.admin_service.dto.order;

import lombok.Data;

@Data
public class OverviewStatsDTO {
    private Long totalUsers;
    private Long newUsersThisMonth;
    private Long totalStaff;

    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long cancelledOrders;
    private Long shippedOrders;
    private Long deliveredOrders;

    private Double totalRevenue;
    private Double revenueThisMonth;
    private Double revenueToday;

    private Long totalProducts;
    private Long activeProducts;
}
