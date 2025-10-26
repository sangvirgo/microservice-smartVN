package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.UserServiceClient;
import com.smartvn.admin_service.dto.user.UserDTO;
import com.smartvn.admin_service.dto.user.UserStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.enums.UserRole;
import com.smartvn.admin_service.exceptions.BaseAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service xử lý các nghiệp vụ quản lý người dùng.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService extends BaseAdminService {

    private final UserServiceClient userServiceClient;

    /**
     * Tìm kiếm người dùng theo các tiêu chí.
     */
    public Page<UserDTO> searchUsers(int page, int size, String search, String role, Boolean isBanned) {
        log.info("Searching users - page: {}, size: {}, search: '{}', role: {}, isBanned: {}", page, size, search, role, isBanned);
        ResponseEntity<ApiResponse<Page<UserDTO>>> response = userServiceClient.searchUsers(page, size, search, role, isBanned);
        return handlePageResponse(response, "Failed to search users");
    }

    /**
     * Lấy thông tin chi tiết người dùng.
     */
    public UserDTO getUserById(Long userId) {
        log.info("Getting user details for ID: {}", userId);
        UserDTO response = userServiceClient.getUserById(userId);
        log.info("userDTO(admin service): {}", response);
        return response;
    }

    /**
     * Cấm người dùng.
     */
    public void banUser(Long userId) {
        log.warn("Banning user with ID: {}", userId);
        ResponseEntity<ApiResponse<Void>> response = userServiceClient.banUser(userId);
        handleResponse(response, "Failed to ban user: " + userId);
        log.info("Successfully banned user with ID: {}", userId);
    }

    /**
     * Bỏ cấm người dùng.
     */
    public void unbanUser(Long userId) {
        log.info("Unbanning user with ID: {}", userId);
        ResponseEntity<ApiResponse<Void>> response = userServiceClient.unbanUser(userId);
        handleResponse(response, "Failed to unban user: " + userId);
        log.info("Successfully unbanned user with ID: {}", userId);
    }


    /**
     * Cảnh cáo người dùng (tăng warning count).
     */
    public void warnUser(Long userId) {
        log.warn("Warning user with ID: {}", userId);
        ResponseEntity<ApiResponse<Void>> response = userServiceClient.warnUser(userId);
        handleResponse(response, "Failed to warn user: " + userId);
        log.info("Successfully warned user with ID: {}", userId);
    }


    public void changeRole(Long userId, UserRole role) {
        log.warn("Changing role for user ID: {} to role: {}", userId, role);
        ResponseEntity<ApiResponse<Void>> response=userServiceClient.changeRole(userId, role);
        handleResponse(response, "Failed to change role for user ID: " + userId);
        log.info("Successfully changed role for user ID: {} to role: {}", userId, role);
    }

    /**
     * Lấy thống kê người dùng.
     */
    public UserStatsDTO getUserStats() {
        log.info("Getting user statistics");
        ResponseEntity<ApiResponse<UserStatsDTO>> response = userServiceClient.getUserStats();
        return handleResponse(response, "Failed to get user stats");
    }

}