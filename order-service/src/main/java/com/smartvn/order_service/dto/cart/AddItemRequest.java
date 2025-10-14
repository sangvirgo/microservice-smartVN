package com.smartvn.order_service.dto.cart;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemRequest {
    private Long productId;
    private String size;
    private int quantity;
}
