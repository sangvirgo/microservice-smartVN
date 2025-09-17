package com.webanhang.team_project.controller.seller;

import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.dto.seller.SellerDashboardDTO;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.service.seller.ISellerDashboardService;
import com.webanhang.team_project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/seller/dashboard")
public class SellerDashboardController {

    private final ISellerDashboardService sellerDashboardService;
    private final UserService userService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse> getDashboardOverview(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        SellerDashboardDTO dashboardData = sellerDashboardService.getDashboardData(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(dashboardData, "Lấy dữ liệu tổng quan thành công"));
    }

    @GetMapping("/revenue/month")
    public ResponseEntity<ApiResponse> getMonthlyRevenue(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        var revenueData = sellerDashboardService.getMonthlyRevenue(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(revenueData, "Lấy dữ liệu doanh thu thành công"));
    }

    @GetMapping("/orders/stats")
    public ResponseEntity<ApiResponse> getOrderStats(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        var orderStats = sellerDashboardService.getOrderStats(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(orderStats, "Lấy thống kê đơn hàng thành công"));
    }

    @GetMapping("/products/stats")
    public ResponseEntity<ApiResponse> getProductStats(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        var productStats = sellerDashboardService.getProductStats(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(productStats, "Lấy thống kê sản phẩm thành công"));
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse> getDailyRevenue(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        var revenueData = sellerDashboardService.getDailyRevenue(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(revenueData, "Lấy dữ liệu doanh thu theo ngày thành công"));
    }

    @GetMapping("/revenue/category")
    public ResponseEntity<ApiResponse> getCategoryRevenue(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        var revenueData = sellerDashboardService.getCategoryRevenue(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(revenueData, "Lấy dữ liệu doanh thu theo danh mục thành công"));
    }
}
