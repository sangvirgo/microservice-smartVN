package com.smartvn.product_service.client;

import com.smartvn.product_service.dto.ai.HomepageRecommendDTO;
import com.smartvn.product_service.dto.ai.SimilarRecommendDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "recommend-service",
        fallback = RecommendationServiceFallback.class
)
public interface RecommendationServiceClient {

    @GetMapping("/api/v1/internal/recommend/homepage")
    HomepageRecommendDTO getHomepageRecommendations(
            @RequestParam(required = false) Integer user_id,
            @RequestParam(defaultValue = "10") Integer top_k
    );

    @GetMapping("/api/v1/internal/recommend/product-detail/{productId}")
    SimilarRecommendDTO getProductDetailRecommendations(
            @PathVariable("productId") String productId,
            @RequestParam(required = false) Integer user_id,
            @RequestParam(defaultValue = "10") Integer top_k
    );
}