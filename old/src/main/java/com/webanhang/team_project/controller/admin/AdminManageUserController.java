package com.webanhang.team_project.controller.admin;


import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.dto.role.ChangeRoleRequest;
import com.webanhang.team_project.dto.user.UpdateUserInfoRequest;
import com.webanhang.team_project.dto.user.UserDTO;
import com.webanhang.team_project.dto.user.UpdateUserStatusRequest;
import com.webanhang.team_project.service.admin.IAdminManageUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin/users")
public class AdminManageUserController {

    private final IAdminManageUserService adminUserService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {

        Page<UserDTO> users = adminUserService.getAllUsers(page, size, search, role);
        return ResponseEntity.ok(ApiResponse.success(users, "Get all users success"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserDetails(@PathVariable Long userId) {
        UserDTO user = adminUserService.getUserDetails(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "Get user details success"));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody UpdateUserInfoRequest request) {
        UserDTO updatedUser = adminUserService.updateUserInfo(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Update user info success"));
    }

    @PutMapping("/{userId}/change-role")
    public ResponseEntity<ApiResponse> changeUserRole(
            @PathVariable Long userId,
            @RequestBody ChangeRoleRequest request) {

        UserDTO updatedUser = adminUserService.changeUserRole(userId, request.getRole());
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Change user role success"));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request) {

        UserDTO updatedUser = adminUserService.updateUserStatus(userId, request.isActive());
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Update user status success"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Delete user success"));
    }

    @GetMapping("/customers/stats")
    public ResponseEntity<ApiResponse> getCustomerStats() {
        Map<String, Object> stats = adminUserService.getCustomerStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Get customer statistics success"));
    }

    @PutMapping("/{userId}/ban")
    public ResponseEntity<ApiResponse> banUser(
            @PathVariable Long userId,
            @RequestParam boolean banned) {
        UserDTO updatedUser = adminUserService.banUser(userId, banned);
        String message = banned ? "Banned account " + userId +" success" : "Unbanned account "+ userId +" success";
        return ResponseEntity.ok(ApiResponse.success(updatedUser, message));
    }
}
