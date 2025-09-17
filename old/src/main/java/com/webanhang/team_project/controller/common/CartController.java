package com.webanhang.team_project.controller.common;



import com.webanhang.team_project.dto.cart.AddItemRequest;
import com.webanhang.team_project.dto.cart.CartDTO;
import com.webanhang.team_project.dto.response.ApiResponse;

import com.webanhang.team_project.model.Cart;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.service.cart.CartService;
import com.webanhang.team_project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/cart")
public class CartController {
    private final CartService cartService;

    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<CartDTO> findUserCart(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        Cart cart = cartService.findUserCart(user.getId());
        CartDTO cartDTO = new CartDTO(cart);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addItemToCart(@RequestHeader("Authorization") String jwt,
                                                     @RequestBody AddItemRequest req) {
        User user = userService.findUserByJwt(jwt);
        cartService.addCartItem(user.getId(), req);

        ApiResponse res = new ApiResponse();
        res.setMessage("Item added to cart successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<ApiResponse> updateCartItem(@RequestHeader("Authorization") String jwt,
                                                  @PathVariable Long itemId,
                                                  @RequestBody AddItemRequest req) {
        User user = userService.findUserByJwt(jwt);
        Cart cart = cartService.updateCartItem(user.getId(), itemId, req);
        CartDTO cartDTO = new CartDTO(cart);
        return ResponseEntity.ok(ApiResponse.success(cartDTO, "Item updated successfully!"));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<ApiResponse> removeCartItem(@RequestHeader("Authorization") String jwt, @PathVariable Long itemId) {
        User user = userService.findUserByJwt(jwt);
        cartService.removeCartItem(user.getId(), itemId);

        return ResponseEntity.ok(ApiResponse.success(null, "Item removed from cart successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        cartService.clearCart(user.getId());
        
        ApiResponse res = new ApiResponse();
        res.setMessage("Cart cleared successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
