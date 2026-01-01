package com.smartvn.admin_service.client;

import com.smartvn.admin_service.dto.ai.TrainingRequest;
import com.smartvn.admin_service.dto.ai.TrainingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "recommend-service", fallback = RecommendServiceFallback.class)
public interface RecommendServiceClient {

    @PostMapping("/api/v1/internal/recommend/train")
    TrainingResponse triggerTraining(@RequestBody TrainingRequest request);

    @GetMapping("/admin/metrics")
    Map<String, Object> getMetrics();
}