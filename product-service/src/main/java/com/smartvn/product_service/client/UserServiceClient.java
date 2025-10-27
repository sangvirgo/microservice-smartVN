package com.smartvn.product_service.client;

import com.smartvn.product_service.config.FeignClientConfig;
import com.smartvn.product_service.dto.UserInfoDTO;
import com.smartvn.product_service.dto.admin.UserDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

// Update UserServiceClient
@FeignClient(name = "user-service", fallback = UserServiceFallback.class, configuration = FeignClientConfig.class)
public interface UserServiceClient {
    @GetMapping("${api.prefix}/internal/users/{userId}")
    UserInfoDTO getUserInfo(@PathVariable("userId") Long userId);

    /**
     * Lấy thông tin chi tiết một người dùng bằng ID.
     * Endpoint này có thể đã có hoặc cần tạo trong User Service.
     * @param userId ID của người dùng
     * @return UserDTO
     */
    @GetMapping("${api.prefix}/internal/users/admin/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);

    @PutMapping("${api.prefix}/internal/users/{userId}/ban")
    ResponseEntity<ApiResponse<Void>> banUser(@PathVariable("userId") Long userId);


    @PutMapping("${api.prefix}/internal/users/{userId}/warn")
    ResponseEntity<ApiResponse<Void>> warnUser(@PathVariable("userId") Long userId);
}