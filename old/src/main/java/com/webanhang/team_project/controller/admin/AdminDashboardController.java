package com.webanhang.team_project.controller.admin;

import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.service.admin.IAdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin/dashboard")
public class AdminDashboardController {

    private final IAdminDashboardService adminDashboardService;

    @GetMapping("/product-stats")
    public ResponseEntity<ApiResponse> getProductStatistics() {
        Map<String, Object> productStats = adminDashboardService.getProductStatistics();
        return ResponseEntity.ok(ApiResponse.success(productStats, "Get product statistics success"));
    }

    @GetMapping("/revenue-by-time/{period}")
    public ResponseEntity<ApiResponse> getRevenueByPeriodOfTime(@PathVariable String period) {
        Map<String, Object> monthlyData = adminDashboardService.getRevenueAnalytics(period);
        return ResponseEntity.ok(ApiResponse.success(monthlyData, "Get monthly revenue success"));
    }

    @GetMapping("/revenue-by-category")
    public ResponseEntity<ApiResponse> getRevenueByCategory() {
        Map<String, Object> categoryRevenue = adminDashboardService.getCategoryRevenue();
        return ResponseEntity.ok(ApiResponse.success(categoryRevenue, "Get category revenue success"));
    }

    @GetMapping("/recent-orders/{limit}")
    public ResponseEntity<ApiResponse> getRecentOrders(@PathVariable int limit) {
        List<Map<String, Object>> recentOrder = adminDashboardService.getRecentOrders(limit);
        return ResponseEntity.ok(ApiResponse.success(recentOrder, "Get recent order success"));
    }

    @GetMapping("/top-selling-products/{limit}")
    public ResponseEntity<ApiResponse> getTopSellingProduct(@PathVariable int limit) {
        List<Map<String, Object>> topSellingProducts = adminDashboardService.getTopSellingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(topSellingProducts, "Get top selling product success"));
    }

    // add revenue by range of time
    @GetMapping("/revenue-by-date-range")
    public ResponseEntity<ApiResponse> getRevenueByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        List<Map<String, Object>> revenueData = adminDashboardService.getRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(revenueData, "Get revenue by date range success"));
    }
}
