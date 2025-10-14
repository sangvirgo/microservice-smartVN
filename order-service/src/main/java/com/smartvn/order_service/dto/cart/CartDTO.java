package com.smartvn.order_service.dto.cart;

import com.smartvn.order_service.model.Cart;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartDTO {
    private Long id;
    private Long userId;
    private Integer totalOriginalPrice;
    private Integer totalItems;
    private Integer totalDiscountedPrice;
    private Integer discount;
    private List<CartItemDTO> cartItems;

    /**
     * Constructor từ Cart entity
     */
    public CartDTO(Cart cart) {
        this.id = cart.getId();
        this.userId = cart.getUserId();
        this.totalItems = cart.getTotalItems();
        this.totalDiscountedPrice = cart.getTotalDiscountedPrice();
        this.totalOriginalPrice = cart.getOriginalPrice();
        this.discount = cart.getDiscount();

        if (cart.getCartItems() != null) {
            this.cartItems = cart.getCartItems().stream()
                    .map(CartItemDTO::new)
                    .collect(Collectors.toList());
        } else {
            this.cartItems = new ArrayList<>();
        }
    }

    /**
     * Constructor mặc định
     */
    public CartDTO() {
        this.cartItems = new ArrayList<>();
    }
}