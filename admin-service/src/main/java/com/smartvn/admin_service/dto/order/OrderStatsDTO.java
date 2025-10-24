package com.smartvn.admin_service.dto.order;

import lombok.Data;

@Data
public class OrderStatsDTO {
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;

    private Double totalRevenue;
    private Double revenueThisMonth;
    private Double revenueToday;

    private Double averageOrderValue;
}