package com.webanhang.team_project.service.seller;

import com.webanhang.team_project.dto.seller.SellerDashboardDTO;

import java.math.BigDecimal;
import java.util.Map;

public interface ISellerDashboardService {
    SellerDashboardDTO getDashboardData(Long sellerId);
    Map<String, BigDecimal> getMonthlyRevenue(Long sellerId);
    Map<String, Integer> getOrderStats(Long sellerId);
    Map<String, Integer> getProductStats(Long sellerId);

    Map<String, BigDecimal> getDailyRevenue(Long sellerId);
    Map<String, BigDecimal> getCategoryRevenue(Long sellerId);
}