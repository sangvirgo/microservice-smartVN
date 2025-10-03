package com.smartvn.product_service.client;

import com.smartvn.product_service.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Update UserServiceClient
@FeignClient(name = "user-service", fallback = UserServiceFallback.class)
public interface UserServiceClient {
    @GetMapping("/api/v1/internal/users/{userId}")
    UserInfoDTO getUserInfo(@PathVariable("userId") Long userId);
}