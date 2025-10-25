package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.OrderServiceClient;
import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.client.UserServiceClient;
import com.smartvn.admin_service.dto.order.OrderStatsDTO;
import com.smartvn.admin_service.dto.order.OverviewStatsDTO;
import com.smartvn.admin_service.dto.product.ProductStatsDTO;
import com.smartvn.admin_service.dto.user.UserStatsDTO;
import com.smartvn.admin_service.exceptions.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    private final UserServiceClient userServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;

    public OverviewStatsDTO getOverview() {
        OverviewStatsDTO stats = new OverviewStatsDTO();

        try {
            // ✅ 1. User stats
            UserStatsDTO userStats = userServiceClient.getUserStats()
                    .getBody().getData();
            stats.setTotalUsers(userStats.getTotalUsers());
            stats.setNewUsersThisMonth(userServiceClient.getNewUsersThisMonth());

            // ✅ 2. Order stats
            OrderStatsDTO orderStats = orderServiceClient.getOrderStats(null, null)
                    .getBody().getData();
            stats.setTotalOrders(orderStats.getTotalOrders());
            stats.setPendingOrders(orderStats.getPendingOrders());
            stats.setTotalRevenue(orderStats.getTotalRevenue());
            stats.setRevenueThisMonth(orderStats.getRevenueThisMonth());

            // ✅ 3. Product stats (cần thêm endpoint trong ProductService)
            ProductStatsDTO productStats = productServiceClient.getProductStats()
                    .getBody().getData();
            stats.setTotalProducts(productStats.getTotalProducts());
            stats.setActiveProducts(productStats.getActiveProducts());

        } catch (Exception e) {
            log.error("Error fetching dashboard data: {}", e.getMessage());
            throw new AppException("Cannot load dashboard", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return stats;
    }
}