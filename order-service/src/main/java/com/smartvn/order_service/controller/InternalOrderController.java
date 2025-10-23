package com.smartvn.order_service.controller;


import com.smartvn.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/internal/orders")
public class InternalOrderController {
    private final OrderRepository  orderRepository;

    @GetMapping("/users/{userId}/products/{productId}/purchased")
    public ResponseEntity<Boolean> hasUserPurchasedProduct(@PathVariable Long userId, @PathVariable Long productId) {
        boolean hasPurchased = orderRepository.existsByUserIdAndProductIdAndDelivered(userId, productId);
        return  ResponseEntity.ok(hasPurchased);
    }
}
