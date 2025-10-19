package com.smartvn.order_service.controller;



import com.smartvn.order_service.dto.cart.AddItemRequest;
import com.smartvn.order_service.dto.cart.CartDTO;
import com.smartvn.order_service.dto.response.ApiResponse;

import com.smartvn.order_service.exceptions.AppException;
import com.smartvn.order_service.model.Cart;
import com.smartvn.order_service.service.CartService;
import com.smartvn.order_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/cart")
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyCart(@RequestHeader("Authorization") String jwt) {
        try {
            Long userId = userService.getUserIdFromJwt(jwt);
            Cart cart = cartService.getCart(userId);
            CartDTO cartDTO =new CartDTO(cart);
            return new ResponseEntity<>(cartDTO, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get cart", "message", e.getMessage()));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(@RequestHeader("Authorization") String jwt,
                                                     @RequestBody AddItemRequest req) {
        try {
            if (req.getProductId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Product ID is required"));
            }
            if (req.getQuantity() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Quantity must be greater than 0"));
            }
            if(req.getSize() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Size is required"));
            }

            Long userId = userService.getUserIdFromJwt(jwt);
            Cart cart = cartService.addCartItem(userId, req);
            CartDTO cartDTO = new CartDTO(cart);

            return ResponseEntity.ok(Map.of(
                    "message", "Item added successfully",
                    "cart", cartDTO
            ));
        } catch (AppException e) {
            return ResponseEntity
                    .status(e.getStatus())
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add item", "message", e.getMessage()));
        }
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItem(@RequestHeader("Authorization") String jwt,
                                                  @PathVariable Long itemId,
                                                  @RequestBody AddItemRequest req) {
        try {
            if (req.getQuantity() < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Quantity must be greater than 0"));
            }

            Long userId = userService.getUserIdFromJwt(jwt);

            if(req.getQuantity() == 0) {
                cartService.removeCartItem(userId, itemId);
                return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
            }

            Cart cart = cartService.updateCartItem(userId, itemId, req);
            CartDTO cartDTO = new CartDTO(cart);

            return ResponseEntity.ok(Map.of(
                    "message", "Item updated successfully",
                    "cart", cartDTO
            ));
        } catch (AppException e) {
            return ResponseEntity
                    .status(e.getStatus())
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update item", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(@RequestHeader("Authorization") String jwt, @PathVariable Long itemId) {
        try {
            Long userId = userService.getUserIdFromJwt(jwt);
            cartService.removeCartItem(userId, itemId);

            return ResponseEntity.ok(Map.of(
                    "message", "Item removed successfully"
            ));
        } catch (AppException e) {
            return ResponseEntity
                    .status(e.getStatus())
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to remove item", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestHeader("Authorization") String jwt) {
        try {
            Long userId = userService.getUserIdFromJwt(jwt);
            cartService.clearCart(userId);

            return ResponseEntity.ok(Map.of(
                    "message", "Cart cleared successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to clear cart", "message", e.getMessage()));
        }
    }
}
