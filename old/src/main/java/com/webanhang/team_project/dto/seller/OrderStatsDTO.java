package com.webanhang.team_project.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsDTO {
    private Long orderId;
    private String customerName;
    private String orderDate;
    private Integer totalAmount;
    private String status;
}
