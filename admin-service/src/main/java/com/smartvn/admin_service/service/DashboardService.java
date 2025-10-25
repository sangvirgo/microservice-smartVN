package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.OrderServiceClient;
import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.client.UserServiceClient;
import com.smartvn.admin_service.dto.dashboard.RevenueChartDTO;
import com.smartvn.admin_service.dto.order.OrderStatsDTO;
import com.smartvn.admin_service.dto.order.OverviewStatsDTO;
import com.smartvn.admin_service.dto.product.ProductStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.dto.user.UserStatsDTO;
import com.smartvn.admin_service.exceptions.AppException;
import com.smartvn.admin_service.exceptions.BaseAdminService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// ✅ DashboardService được cải thiện
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService extends BaseAdminService {

    private final UserServiceClient userServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;

    @CircuitBreaker(name = "dashboardService", fallbackMethod = "getOverviewFallback")
    @Retry(name = "dashboardService")
    public OverviewStatsDTO getOverview() {
        OverviewStatsDTO stats = new OverviewStatsDTO();

        // ✅ PARALLEL FETCH với proper exception handling
        CompletableFuture<Void> userStatsFuture = fetchUserStats(stats);
        CompletableFuture<Void> orderStatsFuture = fetchOrderStats(stats);
        CompletableFuture<Void> productStatsFuture = fetchProductStats(stats);

        // ✅ WAIT và handle exceptions
        try {
            CompletableFuture.allOf(userStatsFuture, orderStatsFuture, productStatsFuture)
                    .join();
        } catch (Exception e) {
            log.error("Error fetching overview stats", e);
            // Circuit breaker sẽ trigger fallback
            throw e;
        }

        return stats;
    }

    private CompletableFuture<Void> fetchUserStats(OverviewStatsDTO stats) {
        return CompletableFuture.runAsync(() -> {
            try {
                ResponseEntity<ApiResponse<UserStatsDTO>> response =
                        userServiceClient.getUserStats();
                UserStatsDTO userStats = handleResponse(response, "Failed to fetch user stats");

                stats.setTotalUsers(userStats.getTotalUsers());

                // ✅ Safe fetch new users
                try {
                    Long newUsers = userServiceClient.getNewUsersThisMonth();
                    stats.setNewUsersThisMonth(newUsers != null ? newUsers : 0L);
                } catch (Exception e) {
                    log.warn("Failed to fetch new users count", e);
                    stats.setNewUsersThisMonth(0L);
                }
            } catch (Exception e) {
                log.error("Error fetching user stats", e);
                stats.setTotalUsers(0L);
                stats.setNewUsersThisMonth(0L);
            }
        });
    }

    private CompletableFuture<Void> fetchOrderStats(OverviewStatsDTO stats) {
        return CompletableFuture.runAsync(() -> {
            try {
                ResponseEntity<ApiResponse<OrderStatsDTO>> response =
                        orderServiceClient.getOrderStats(null, null);
                OrderStatsDTO orderStats = handleResponse(response, "Failed to fetch order stats");

                stats.setTotalOrders(orderStats.getTotalOrders());
                stats.setPendingOrders(orderStats.getPendingOrders());
                stats.setTotalRevenue(orderStats.getTotalRevenue());
                stats.setRevenueThisMonth(orderStats.getRevenueThisMonth());
            } catch (Exception e) {
                log.error("Error fetching order stats", e);
                setDefaultOrderStats(stats);
            }
        });
    }

    // ✅ FALLBACK METHOD
    public OverviewStatsDTO getOverviewFallback(Throwable t) {
        log.error("Dashboard circuit breaker activated", t);
        OverviewStatsDTO fallback = new OverviewStatsDTO();
        // Set all to 0
        return fallback;
    }
}