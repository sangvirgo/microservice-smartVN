package com.smartvn.product_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", fallback = OrderServiceFallback.class)
public interface OrderServiceClient {

    @GetMapping("${api.prefix}/internal/orders/users/{userId}/products/{productId}/purchased")
    Boolean hasUserPurchasedProduct(@PathVariable("userId") Long userId, @PathVariable("productId") Long productId);
}
