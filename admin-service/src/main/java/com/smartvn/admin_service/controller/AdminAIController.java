package com.smartvn.admin_service.controller;

import com.smartvn.admin_service.client.RecommendServiceClient;
import com.smartvn.admin_service.dto.ai.TrainingRequest;
import com.smartvn.admin_service.dto.ai.TrainingResponse;
import com.smartvn.admin_service.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("${api.prefix}/admin/ai")
@RequiredArgsConstructor
public class AdminAIController {
    private final RecommendServiceClient recommendServiceClient;

    @PostMapping("/retrain")
    public ResponseEntity<ApiResponse<TrainingResponse>> triggerTraining(
            @RequestBody TrainingRequest request) {

        try {
            TrainingResponse response = recommendServiceClient.triggerTraining(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Training started"));
        } catch (Exception e) {
            log.error("Failed to trigger training", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("AI service unavailable"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAIStatus() {
        try {
            Map<String, Object> metrics = recommendServiceClient.getMetrics();
            return ResponseEntity.ok(ApiResponse.success(metrics, "AI status"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("AI service unavailable"));
        }
    }
}
