package com.smartvn.product_service.client;

import com.smartvn.product_service.dto.UserInfoDTO;
import com.smartvn.product_service.dto.admin.UserDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceFallback implements UserServiceClient {

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        log.warn("Fallback: Cannot fetch user info for userId {}. Returning anonymous user.", userId);

        UserInfoDTO fallbackUser = new UserInfoDTO();
        fallbackUser.setId(userId);
        fallbackUser.setFirstName("Anonymous");
        fallbackUser.setLastName("User");
        fallbackUser.setAvatar(null);

        return fallbackUser;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        log.warn("Fallback: Cannot fetch user dto for userId {}. Returning anonymous user.", userId);
        UserDTO fallbackUser = new UserDTO();
        fallbackUser.setId(userId);
        fallbackUser.setFirstName("Anonymous");
        fallbackUser.setLastName("User");
        fallbackUser.setWarningCount(0);
        fallbackUser.setEmail(null);
        return fallbackUser;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> banUser(Long userId) {
        log.error("User Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("User service đang bảo trì. Vui lòng thử lại sau."));
    }


    @Override
    public ResponseEntity<ApiResponse<Void>> warnUser(Long userId) {
        log.error("User Service unavailable. Cannot warn user {}", userId);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("User service đang bảo trì. Vui lòng thử lại sau."));
    }
}