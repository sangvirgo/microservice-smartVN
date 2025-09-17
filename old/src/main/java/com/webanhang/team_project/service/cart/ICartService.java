package com.webanhang.team_project.service.cart;


import com.webanhang.team_project.dto.cart.AddItemRequest;
import com.webanhang.team_project.model.Cart;
import com.webanhang.team_project.model.User;

public interface ICartService {

    public Cart createCart(User user);

    public Cart findUserCart(Long userId);

    public Cart addCartItem(Long userId, AddItemRequest req);

    public Cart updateCartItem(Long userId, Long itemId, AddItemRequest req);

    public void removeCartItem(Long userId, Long itemId);

    public void clearCart(Long userId);

    public Cart getCartByUserId(Long userId);

    public Cart initializeNewCartForUser(User user);
}

