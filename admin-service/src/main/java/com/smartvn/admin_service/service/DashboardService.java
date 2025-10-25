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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
public class DashboardService {
    private final UserServiceClient userServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;

    @CircuitBreaker(name = "dashboardService", fallbackMethod = "getOverviewFallback")
    public OverviewStatsDTO getOverview() {
        OverviewStatsDTO stats = new OverviewStatsDTO();

        // ✅ PARALLEL FETCH với CompletableFuture
        CompletableFuture<Void> userStatsFuture = CompletableFuture.runAsync(() -> {
            try {
                UserStatsDTO userStats = fetchUserStats();
                stats.setTotalUsers(userStats.getTotalUsers());
                stats.setNewUsersThisMonth(fetchNewUsersThisMonth());
            } catch (Exception e) {
                log.error("Error fetching user stats", e);
                stats.setTotalUsers(0L);
                stats.setNewUsersThisMonth(0L);
            }
        });

        CompletableFuture<Void> orderStatsFuture = CompletableFuture.runAsync(() -> {
            try {
                OrderStatsDTO orderStats = fetchOrderStats();
                stats.setTotalOrders(orderStats.getTotalOrders());
                stats.setPendingOrders(orderStats.getPendingOrders());
                stats.setTotalRevenue(orderStats.getTotalRevenue());
                stats.setRevenueThisMonth(orderStats.getRevenueThisMonth());
            } catch (Exception e) {
                log.error("Error fetching order stats", e);
                setDefaultOrderStats(stats);
            }
        });

        CompletableFuture<Void> productStatsFuture = CompletableFuture.runAsync(() -> {
            try {
                ProductStatsDTO productStats = fetchProductStats();
                stats.setTotalProducts(productStats.getTotalProducts());
                stats.setActiveProducts(productStats.getActiveProducts());
            } catch (Exception e) {
                log.error("Error fetching product stats", e);
                stats.setTotalProducts(0L);
                stats.setActiveProducts(0L);
            }
        });

        // ✅ WAIT for all futures
        CompletableFuture.allOf(userStatsFuture, orderStatsFuture, productStatsFuture)
                .join();

        return stats;
    }

    // ✅ EXTRACT methods để dễ test
    private UserStatsDTO fetchUserStats() {
        return Optional.ofNullable(userServiceClient.getUserStats())
                .map(ResponseEntity::getBody)
                .map(ApiResponse::getData)
                .orElseThrow(() -> new AppException(
                        "User stats unavailable",
                        HttpStatus.SERVICE_UNAVAILABLE));
    }

    private Long fetchNewUsersThisMonth() {
        return Optional.ofNullable(userServiceClient.getNewUsersThisMonth())
                .map(ResponseEntity::getBody)
                .orElse(0L);
    }

    private OrderStatsDTO fetchOrderStats() {
        return Optional.ofNullable(
                        orderServiceClient.getOrderStats(null, null))
                .map(ResponseEntity::getBody)
                .map(ApiResponse::getData)
                .orElseThrow(() -> new AppException(
                        "Order stats unavailable",
                        HttpStatus.SERVICE_UNAVAILABLE));
    }

    private ProductStatsDTO fetchProductStats() {
        return Optional.ofNullable(productServiceClient.getProductStats())
                .map(ResponseEntity::getBody)
                .map(ApiResponse::getData)
                .orElseThrow(() -> new AppException(
                        "Product stats unavailable",
                        HttpStatus.SERVICE_UNAVAILABLE));
    }

    private void setDefaultOrderStats(OverviewStatsDTO stats) {
        stats.setTotalOrders(0L);
        stats.setPendingOrders(0L);
        stats.setTotalRevenue(0.0);
        stats.setRevenueThisMonth(0.0);
    }

    // ✅ FALLBACK method
    public OverviewStatsDTO getOverviewFallback(Throwable t) {
        log.error("Dashboard service circuit breaker activated", t);
        OverviewStatsDTO fallback = new OverviewStatsDTO();
        // Set all to 0
        fallback.setTotalUsers(0L);
        fallback.setNewUsersThisMonth(0L);
        fallback.setTotalOrders(0L);
        fallback.setPendingOrders(0L);
        fallback.setTotalRevenue(0.0);
        fallback.setRevenueThisMonth(0.0);
        fallback.setTotalProducts(0L);
        fallback.setActiveProducts(0L);
        return fallback;
    }
}