package com.webanhang.team_project.service.cart;


import com.webanhang.team_project.exceptions.GlobalExceptionHandler;
import com.webanhang.team_project.model.Cart;
import com.webanhang.team_project.model.CartItem;
import com.webanhang.team_project.model.Product;

public interface ICartItemService {
    public CartItem createCartItem(CartItem cartItem);
    public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) ;
    public void deleteAllCartItems(Long cartId, Long userId) ;
    public CartItem isCartItemExist(Cart cart, Product product, String size, Long userId) ;
    public CartItem findCartItemById(Long cartItemId) ;
    CartItem addCartItem(CartItem cartItem);
    CartItem updateCartItem(Long cartItemId, CartItem cartItem) ;
    void deleteCartItem(Long cartItemId) ;
    CartItem getCartItemById(Long cartItemId) ;
    boolean isCartItemExist(Long cartId, Long productId, String size);
    
    // Thêm các phương thức mới
    CartItem addItemToCart(Long cartId, Long productId, int quantity);
    void removeItemFromCart(Long cartId, Long itemId);
    CartItem updateItemQuantity(Long cartId, Long itemId, int quantity);
}
