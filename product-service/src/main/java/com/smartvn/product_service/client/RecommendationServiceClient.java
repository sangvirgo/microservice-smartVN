package com.smartvn.product_service.client;

import com.smartvn.product_service.config.FeignClientConfig;
import com.smartvn.product_service.dto.ai.HomepageRecommendDTO;
import com.smartvn.product_service.dto.ai.SimilarRecommendDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "RECOMMEND-SERVICE",
        configuration = FeignClientConfig.class,  // ✅ THÊM DÒNG NÀY (để auto inject X-API-KEY)
        fallback = RecommendationServiceFallback.class
)
public interface RecommendationServiceClient {

    // ✅ ĐỔI Integer → Long
    @GetMapping("/api/v1/internal/recommend/homepage")
    HomepageRecommendDTO getHomepageRecommendations(
            @RequestParam(name = "user_id", required = false) Long userId,  // ✅ LONG
            @RequestParam(name = "top_k", defaultValue = "10") int topK
    );

    @GetMapping("/api/v1/internal/recommend/product-detail/{product_id}")
    SimilarRecommendDTO getProductDetailRecommendations(
            @PathVariable("product_id") String productId,
            @RequestParam(name = "user_id", required = false) Long userId,  // ✅ LONG
            @RequestParam(name = "top_k", defaultValue = "10") int topK
    );
}
