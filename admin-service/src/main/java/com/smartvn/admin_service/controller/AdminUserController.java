package com.smartvn.admin_service.controller;

import com.smartvn.admin_service.client.UserServiceClient;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.dto.user.UserDTO;
import com.smartvn.admin_service.dto.user.UserStatsDTO;
import com.smartvn.admin_service.enums.UserRole;
import com.smartvn.admin_service.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isBanned) {

        Page<UserDTO> users = adminUserService.searchUsers(page, size, search, role, isBanned);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved"));
    }

    @PutMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<?>> banUser(@PathVariable Long id) {
        adminUserService.banUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User banned"));
    }

    @PutMapping("/{id}/unban")
    public ResponseEntity<ApiResponse<?>> unbanUser(@PathVariable Long id) {
        adminUserService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User unbanned"));
    }

    @PutMapping("/{id}/warn")
    public ResponseEntity<ApiResponse<?>> warnUser(@PathVariable Long id) {
        adminUserService.warnUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Warning added"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsDTO>> getStats() {
        UserStatsDTO stats = adminUserService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Stats retrieved"));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<?>> changeRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request
    ) {
        String roleStr = request.get("role");

        if (roleStr == null || roleStr.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Role is required"));
        }

        try {
            UserRole role = UserRole.valueOf(roleStr.toUpperCase());
            adminUserService.changeRole(userId, role);
            return ResponseEntity.ok(ApiResponse.success(null, "Role changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid role. Allowed values: ADMIN, STAFF, CUSTOMER"));
        }
    }
}