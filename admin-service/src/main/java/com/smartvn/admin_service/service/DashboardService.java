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
import com.smartvn.admin_service.exceptions.BaseAdminService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

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

        // ✅ PARALLEL FETCH - Gọi 3 API cùng lúc
        CompletableFuture<UserStatsDTO> userStatsFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return handleResponse(
                                userServiceClient.getUserStats(),
                                "Failed to fetch user stats"
                        );
                    } catch (Exception e) {
                        log.error("Error fetching user stats", e);
                        return getDefaultUserStats();
                    }
                });

        CompletableFuture<OrderStatsDTO> orderStatsFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return handleResponse(
                                orderServiceClient.getOrderStats(null, null),
                                "Failed to fetch order stats"
                        );
                    } catch (Exception e) {
                        log.error("Error fetching order stats", e);
                        return getDefaultOrderStats();
                    }
                });

        CompletableFuture<ProductStatsDTO> productStatsFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return handleResponse(
                                productServiceClient.getProductStats(),
                                "Failed to fetch product stats"
                        );
                    } catch (Exception e) {
                        log.error("Error fetching product stats", e);
                        return getDefaultProductStats();
                    }
                });

        // ✅ WAIT ALL & COMBINE
        try {
            CompletableFuture.allOf(userStatsFuture, orderStatsFuture, productStatsFuture).join();

            UserStatsDTO userStats = userStatsFuture.get();
            OrderStatsDTO orderStats = orderStatsFuture.get();
            ProductStatsDTO productStats = productStatsFuture.get();

            // Map vào OverviewStatsDTO
            stats.setTotalUsers(userStats.getTotalUsers());
            stats.setTotalStaff(userStats.getTotalStaff());

            stats.setTotalOrders(orderStats.getTotalOrders());
            stats.setPendingOrders(orderStats.getPendingOrders());
            stats.setConfirmedOrders(orderStats.getConfirmedOrders());
            stats.setShippedOrders(orderStats.getShippedOrders());
            stats.setDeliveredOrders(orderStats.getDeliveredOrders());
            stats.setCancelledOrders(orderStats.getCancelledOrders());
            stats.setTotalRevenue(orderStats.getTotalRevenue());
            stats.setRevenueThisMonth(orderStats.getRevenueThisMonth());
            stats.setRevenueToday(orderStats.getRevenueToday());

            stats.setTotalProducts(productStats.getTotalProducts());
            stats.setActiveProducts(productStats.getActiveProducts());

            // ✅ NEW: Fetch new users count riêng (vì nó trả về Long)
            try {
                Long newUsers = userServiceClient.getNewUsersThisMonth();
                stats.setNewUsersThisMonth(newUsers != null ? newUsers : 0L);
            } catch (Exception e) {
                log.warn("Failed to fetch new users count", e);
                stats.setNewUsersThisMonth(0L);
            }

        } catch (Exception e) {
            log.error("Error combining stats", e);
            throw new RuntimeException("Error combining stats: "  + e.getMessage());
        }

        return stats;
    }

    // ✅ THÊM FALLBACK METHOD cho getOverview
    public OverviewStatsDTO getOverviewFallback(Throwable t) {
        log.error("Dashboard overview circuit breaker activated", t);

        OverviewStatsDTO fallback = new OverviewStatsDTO();
        // Set tất cả về 0 để tránh null
        fallback.setTotalUsers(0L);
        fallback.setNewUsersThisMonth(0L);
        fallback.setTotalStaff(0L);
        fallback.setTotalOrders(0L);
        fallback.setPendingOrders(0L);
        fallback.setConfirmedOrders(0L);
        fallback.setShippedOrders(0L);
        fallback.setDeliveredOrders(0L);
        fallback.setCancelledOrders(0L);
        fallback.setTotalRevenue(0.0);
        fallback.setRevenueThisMonth(0.0);
        fallback.setRevenueToday(0.0);
        fallback.setTotalProducts(0L);
        fallback.setActiveProducts(0L);

        return fallback;
    }

    // ✅ Helper methods để tránh null
    private UserStatsDTO getDefaultUserStats() {
        UserStatsDTO stats = new UserStatsDTO();
        stats.setTotalUsers(0L);
        stats.setTotalStaff(0L);
        return stats;
    }

    private OrderStatsDTO getDefaultOrderStats() {
        OrderStatsDTO stats = new OrderStatsDTO();
        stats.setTotalOrders(0L);
        stats.setPendingOrders(0L);
        stats.setTotalRevenue(0.0);
        stats.setRevenueThisMonth(0.0);
        stats.setRevenueToday(0.0);
        return stats;
    }

    private ProductStatsDTO getDefaultProductStats() {
        ProductStatsDTO stats = new ProductStatsDTO();
        stats.setTotalProducts(0L);
        stats.setActiveProducts(0L);
        return stats;
    }


    @CircuitBreaker(name = "dashboardService", fallbackMethod = "getRevenueChartFallback")
    @Retry(name = "dashboardService")
    public RevenueChartDTO getRevenueChart(LocalDate startDate, LocalDate endDate) {
        try {
            ResponseEntity<ApiResponse<RevenueChartDTO>> response =
                    orderServiceClient.getRevenueChart(startDate, endDate);

            return handleResponse(response, "Failed to fetch revenue chart");
        } catch (Exception e) {
            log.error("Error fetching revenue chart", e);
            throw e;
        }
    }

    /**
     * ✅ FALLBACK METHOD cho getRevenueChart
     */
    public RevenueChartDTO getRevenueChartFallback(LocalDate startDate, LocalDate endDate, Throwable t) {
        log.error("Revenue chart circuit breaker activated", t);

        RevenueChartDTO fallback = new RevenueChartDTO();
        fallback.setDataPoints(Collections.emptyList());
        fallback.setTotalRevenue(0.0);

        return fallback;
    }
}