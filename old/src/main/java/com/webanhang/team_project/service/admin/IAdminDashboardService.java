package com.webanhang.team_project.service.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.webanhang.team_project.dto.order.OrderDTO;
import com.webanhang.team_project.dto.product.ProductDTO;
import com.webanhang.team_project.dto.seller.SellerRevenueDTO;

public interface IAdminDashboardService {
    // 1. API cho thống kê sản phẩm (mẫu sản phẩm, trong kho, đã bán)
    Map<String, Object> getProductStatistics();

    // 2. API cho doanh thu theo thời gian (kết hợp tuần và tháng)
    Map<String, Object> getRevenueAnalytics(String period); // period: "week" hoặc "month"

    // 3. API cho phân bổ doanh thu theo danh mục (top 3 + others)
    Map<String, Object> getCategoryRevenue();

    // 4. API cho đơn hàng gần đây
    List<Map<String, Object>> getRecentOrders(int limit);

    // 5. API cho sản phẩm bán chạy
    List<Map<String, Object>> getTopSellingProducts(int limit);

    List<Map<String, Object>> getRevenueByDateRange(LocalDate startDate, LocalDate endDate);
}