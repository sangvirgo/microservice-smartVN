package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.OrderServiceClient;
import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.client.UserServiceClient;
import com.smartvn.admin_service.dto.order.OrderStatsDTO;
import com.smartvn.admin_service.dto.user.UserStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        // User stats
        UserStatsDTO userStats = userServiceClient.getUserStats()
                .getBody().getData();
        stats.setTotalUsers(userStats.getTotalUsers());

        // Order stats
        OrderStatsDTO orderStats = orderServiceClient.getOrderStats(null, null)
                .getBody().getData();
        stats.setTotalOrders(orderStats.getTotalOrders());
        stats.setTotalRevenue(orderStats.getTotalRevenue());

        return stats;
    }
}