package com.webanhang.team_project.dto.cart;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemRequest {
    private Long productId;
    private String size;
    private int quantity;
}
