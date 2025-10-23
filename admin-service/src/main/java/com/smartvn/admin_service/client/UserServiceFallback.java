package com.smartvn.admin_service.client;

import com.smartvn.admin_service.dto.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import com.smartvn.admin_service.dto.user.*; // Cần tạo DTO này

public class UserServiceFallback implements UserServiceClient{
    @Override
    public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(int page, int size, String search, String role, Boolean isBanned) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> banUser(Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> unbanUser(Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> warnUser(Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<UserStatsDTO>> getUserStats() {
        return null;
    }
}
