package com.smartvn.order_service.dto.cart;

import com.smartvn.order_service.enums.PaymentMethod;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Long addressId;
    private List<Long> cartItemIds; // ✅ Danh sách ID các item muốn checkout
}