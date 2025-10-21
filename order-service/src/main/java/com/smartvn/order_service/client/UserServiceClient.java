package com.smartvn.order_service.client;

import com.smartvn.order_service.config.FeignClientConfig;
import com.smartvn.order_service.dto.user.AddressDTO;
import com.smartvn.order_service.dto.user.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client để giao tiếp với User Service
 */
@FeignClient(
        name = "user-service",
        fallback = UserServiceFallback.class,
        configuration = FeignClientConfig.class
)
public interface UserServiceClient {

    /**
     * Lấy thông tin user
     */
    @GetMapping("/api/v1/internal/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);

    /**
     * Lấy thông tin địa chỉ
     */
    @GetMapping("/api/v1/internal/users/addresses/{addressId}")
    AddressDTO getAddressById(@PathVariable("addressId") Long addressId);

    /**
     * Validate địa chỉ có thuộc về user không
     */
    @GetMapping("/api/v1/internal/users/{userId}/addresses/{addressId}/validate")
    Boolean validateUserAddress(
            @PathVariable("userId") Long userId,
            @PathVariable("addressId") Long addressId
    );
}