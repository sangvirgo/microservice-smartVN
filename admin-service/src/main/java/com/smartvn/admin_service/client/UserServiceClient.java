package com.smartvn.admin_service.client;

import com.smartvn.admin_service.config.FeignClientConfig;
import com.smartvn.admin_service.dto.user.UserDTO; // Cần tạo DTO này
import com.smartvn.admin_service.dto.response.ApiResponse; // Cần tạo DTO này
import com.smartvn.admin_service.dto.user.UserStatsDTO; // Cần tạo DTO này
import com.smartvn.admin_service.enums.UserRole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page; // Sử dụng Page của Spring Data
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client để giao tiếp với User Service.
 * Sử dụng cấu hình FeignClientConfig để thêm X-API-KEY.
 */
@FeignClient(name = "user-service", configuration = FeignClientConfig.class , fallback = UserServiceFallback.class)
// Thêm fallback nếu cần
public interface UserServiceClient {

    /**
     * Lấy danh sách người dùng với bộ lọc và phân trang.
     * Endpoint này cần được tạo trong User Service (ví dụ: AdminInternalController).
     * @param page Số trang
     * @param size Kích thước trang
     * @param search Từ khóa tìm kiếm (email, tên)
     * @param role Vai trò (CUSTOMER, STAFF) - lọc theo vai trò
     * @param isBanned Lọc theo trạng thái bị cấm
     * @return Trang kết quả UserDTO
     */
    @GetMapping("${api.prefix}/internal/users/search")
    ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "isBanned", required = false) Boolean isBanned);

    /**
     * Lấy thông tin chi tiết một người dùng bằng ID.
     * Endpoint này có thể đã có hoặc cần tạo trong User Service.
     * @param userId ID của người dùng
     * @return UserDTO
     */
    @GetMapping("${api.prefix}/internal/users/admin/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);


    /**
     * Cấm hoặc bỏ cấm một người dùng.
     * Endpoint này cần được tạo trong User Service.
     * @param userId ID của người dùng
     * @return Phản hồi không có nội dung
     */
    @PutMapping("${api.prefix}/internal/users/{userId}/ban")
    ResponseEntity<ApiResponse<Void>> banUser(@PathVariable("userId") Long userId);

    @PutMapping("${api.prefix}/internal/users/{userId}/unban")
    ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable("userId") Long userId);

    /**
     * Tăng số lần cảnh báo cho người dùng.
     * Endpoint này cần được tạo trong User Service.
     * @param userId ID của người dùng
     * @return Phản hồi không có nội dung
     */
    @PutMapping("${api.prefix}/internal/users/{userId}/warn")
    ResponseEntity<ApiResponse<Void>> warnUser(@PathVariable("userId") Long userId);

    /**
     * Lấy thống kê về người dùng.
     * Endpoint này cần được tạo trong User Service.
     * @return UserStatsDTO chứa thông tin thống kê
     */
    @GetMapping("${api.prefix}/internal/users/stats")
    ResponseEntity<ApiResponse<UserStatsDTO>> getUserStats();

    @PutMapping("${api.prefix}/internal/users/{userId}/role")
    ResponseEntity<ApiResponse<Void>> changeRole(@PathVariable("userId") Long userId, @RequestParam UserRole role);

    @GetMapping("${api.prefix}/internal/users/stats/new-this-month")
    Long getNewUsersThisMonth();
}