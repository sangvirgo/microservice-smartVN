package com.smartvn.admin_service.controller;

import com.smartvn.admin_service.dto.dashboard.RevenueChartDTO;
import com.smartvn.admin_service.dto.order.OverviewStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("${api.prefix}/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<OverviewStatsDTO>> getOverview() {
        OverviewStatsDTO stats = dashboardService.getOverview();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard overview"));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueChartDTO>> getRevenue(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        RevenueChartDTO data = dashboardService.getRevenueChart(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(data, "Revenue data"));
    }
}