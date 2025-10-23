package com.smartvn.product_service.client;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderServiceFallback implements OrderServiceClient{
    @Override
    public Boolean hasUserPurchasedProduct(Long userId, Long productId) {
        log.error("Order Service unavailable. Cannot verify purchase");
        return false;
    }
}
