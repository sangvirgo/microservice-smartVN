package com.webanhang.team_project.service.cart;


import com.webanhang.team_project.exceptions.GlobalExceptionHandler;
import com.webanhang.team_project.model.Cart;
import com.webanhang.team_project.model.CartItem;
import com.webanhang.team_project.model.Product;
import com.webanhang.team_project.repository.CartItemRepository;
import com.webanhang.team_project.repository.CartRepository;
import com.webanhang.team_project.service.product.IProductService;
import com.webanhang.team_project.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService {
    private final CartItemRepository cartItemRepository;
    private final IProductService productService;

    @Override
    public CartItem addCartItem(CartItem cartItem) {
        try {
            return cartItemRepository.save(cartItem);
        } catch (Exception e) {
            throw new RuntimeException("Error adding cart item: " + e.getMessage());
        }
    }

    @Override
    public CartItem updateCartItem(Long cartItemId, CartItem cartItem) {
        CartItem existingItem = getCartItemById(cartItemId);
        try {
            cartItem.setId(cartItemId);
            return cartItemRepository.save(cartItem);
        } catch (Exception e) {
            throw new RuntimeException("Error updating cart item: " + e.getMessage());
        }
    }

    @Override
    public void deleteCartItem(Long cartItemId) {
        try {
            cartItemRepository.deleteById(cartItemId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting cart item: " + e.getMessage());
        }
    }

    @Override
    public CartItem getCartItemById(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));
    }

    @Override
    public boolean isCartItemExist(Long cartId, Long productId, String size)  {
        return cartItemRepository.existsByCartIdAndProductIdAndSize(cartId, productId, size);
    }

    @Override
    public CartItem createCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) {
        try {
            CartItem existingItem = getCartItemById(id);
            cartItem.setId(id);
            return cartItemRepository.save(cartItem);
        } catch (Exception e) {
            throw new RuntimeException("Error updating cart item: " + e.getMessage());
        }
    }

    @Override
    public void deleteAllCartItems(Long cartId, Long userId) {
        try {
            cartItemRepository.deleteByCartId(cartId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting all cart items: " + e.getMessage());
        }
    }

    @Override
    public CartItem isCartItemExist(Cart cart, Product product, String size, Long userId) {
        try {
            return cartItemRepository.isCartItemExist(cart, product, size, userId);
        } catch (Exception e) {
            throw new RuntimeException("Error checking cart item existence: " + e.getMessage());
        }
    }

    @Override
    public CartItem findCartItemById(Long cartItemId)  {
        try {
            return getCartItemById(cartItemId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public CartItem addItemToCart(Long cartId, Long productId, int quantity) {
        try {
            // Lấy thông tin sản phẩm
            Product product = productService.findProductById(productId);
            
            // Tạo mục mới trong giỏ hàng
            CartItem cartItem = new CartItem();
            Cart cart = new Cart();
            cart.setId(cartId);
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice());
            cartItem.setDiscountedPrice(product.getDiscountedPrice());
            
            return cartItemRepository.save(cartItem);
        } catch (Exception e) {
            throw new RuntimeException("Error adding item to cart: " + e.getMessage());
        }
    }
    
    @Override
    public void removeItemFromCart(Long cartId, Long itemId) {
        try {
            CartItem cartItem = getCartItemById(itemId);
            if (cartItem.getCart().getId().equals(cartId)) {
                cartItemRepository.deleteById(itemId);
            } else {
                throw new RuntimeException("Cart item does not belong to the specified cart");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error removing item from cart: " + e.getMessage());
        }
    }
    
    @Override
    public CartItem updateItemQuantity(Long cartId, Long itemId, int quantity) {
        try {
            CartItem cartItem = getCartItemById(itemId);
            if (cartItem.getCart().getId().equals(cartId)) {
                cartItem.setQuantity(quantity);
                return cartItemRepository.save(cartItem);
            } else {
                throw new RuntimeException("Cart item does not belong to the specified cart");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating item quantity: " + e.getMessage());
        }
    }
}
