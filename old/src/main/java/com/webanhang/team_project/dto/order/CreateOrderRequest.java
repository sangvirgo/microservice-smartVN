package com.webanhang.team_project.dto.order;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long addressId;
    private String paymentMethod;
    private Long cartId;
} 