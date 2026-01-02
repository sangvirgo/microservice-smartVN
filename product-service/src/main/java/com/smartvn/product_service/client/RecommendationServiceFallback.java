package com.smartvn.product_service.client;

import com.smartvn.product_service.dto.ai.HomepageRecommendDTO;
import com.smartvn.product_service.dto.ai.SimilarRecommendDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class RecommendationServiceFallback implements RecommendationServiceClient {

    @Override
    public HomepageRecommendDTO getHomepageRecommendations(Long userId, int topK) {
        log.error("AI service unavailable - returning empty recommendations");
        return new HomepageRecommendDTO(Collections.emptyList(), "fallback", 0);
    }

    @Override
    public SimilarRecommendDTO getProductDetailRecommendations(String productId, Long userId, int topK) {
        log.error("AI service unavailable - returning empty similar products");
        return new SimilarRecommendDTO(Collections.emptyList(), "fallback", 0);
    }
}