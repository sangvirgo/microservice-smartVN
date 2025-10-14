package com.smartvn.order_service.client;

import com.smartvn.order_service.dto.user.AddressDTO;
import com.smartvn.order_service.dto.user.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback khi User Service không khả dụng
 */
@Component
@Slf4j
public class UserServiceFallback implements UserServiceClient {

    @Override
    public UserDTO getUserById(Long userId) {
        log.error("User Service unavailable. Returning fallback for userId: {}", userId);

        UserDTO fallback = new UserDTO();
        fallback.setId(userId);
        fallback.setFirstName("Unknown");
        fallback.setLastName("User");
        fallback.setEmail("unavailable@system.com");

        return fallback;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        log.error("User Service unavailable. Returning fallback for addressId: {}", addressId);

        AddressDTO fallback = new AddressDTO();
        fallback.setId(addressId);
        fallback.setFullName("Unknown");
        fallback.setProvince("Unknown");
        fallback.setWard("Unknown");
        fallback.setStreet("Unknown");
        fallback.setPhoneNumber("Unknown");

        return fallback;
    }

    @Override
    public Boolean validateUserAddress(Long userId, Long addressId) {
        log.error("User Service unavailable. Cannot validate address {} for user {}", addressId, userId);
        // Trả về false để an toàn
        return false;
    }
}