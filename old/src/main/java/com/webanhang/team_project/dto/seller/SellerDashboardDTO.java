package com.webanhang.team_project.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDashboardDTO {
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer totalProducts;
    private Integer totalCustomers;
    private List<OrderStatsDTO> recentOrders;
    private Map<String, BigDecimal> revenueByWeek;
    private Map<String, BigDecimal> revenueByMonth;
}
