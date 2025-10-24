package com.smartvn.admin_service.client;

import com.smartvn.admin_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.smartvn.admin_service.dto.user.UserDTO;
import com.smartvn.admin_service.dto.user.UserStatsDTO;

@Slf4j
public class UserServiceFallback implements UserServiceClient{
    @Override
    public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(int page, int size, String search, String role, Boolean isBanned) {
        log.error("User Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("User service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(Long userId) {
        log.error("User Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("User service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> banUser(Long userId) {
        log.error("User Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("User service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> unbanUser(Long userId) {
        log.error("User Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("User service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> warnUser(Long userId) {
        log.error("User Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("User service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<UserStatsDTO>> getUserStats() {
        log.error("User Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("User service đang bảo trì. Vui lòng thử lại sau."));    }
}
