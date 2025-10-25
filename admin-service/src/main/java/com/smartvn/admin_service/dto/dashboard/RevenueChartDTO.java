package com.smartvn.admin_service.dto.dashboard;

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
