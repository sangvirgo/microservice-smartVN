package com.smartvn.order_service.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class RevenueChartDTO {
    private List<RevenueDataPoint> dataPoints;
    private Double totalRevenue;

    @Data
    @AllArgsConstructor
    public static class RevenueDataPoint {
        private String date; // hoặc LocalDate
        private Double revenue;
    }
}
