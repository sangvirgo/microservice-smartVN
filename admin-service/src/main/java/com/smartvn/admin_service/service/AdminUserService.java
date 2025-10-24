package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.UserServiceClient;
import com.smartvn.admin_service.dto.user.UserDTO;
import com.smartvn.admin_service.dto.user.UserStatsDTO;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AdminUserService {

    private final UserServiceClient userServiceClient;

    /**
     * Tìm kiếm người dùng theo các tiêu chí.
     */
    public Page<UserDTO> searchUsers(int page, int size, String search, String role, Boolean isBanned) {
        log.info("Searching users - page: {}, size: {}, search: '{}', role: {}, isBanned: {}", page, size, search, role, isBanned);
        ResponseEntity<ApiResponse<Page<UserDTO>>> response = userServiceClient.searchUsers(page, size, search, role, isBanned);
        return handleFeignResponse(response, "Failed to search users");
    }

    /**
     * Lấy thông tin chi tiết người dùng.
     */
    public UserDTO getUserById(Long userId) {
        log.info("Getting user details for ID: {}", userId);
        ResponseEntity<ApiResponse<UserDTO>> response = userServiceClient.getUserById(userId);
        return handleFeignResponse(response, "Failed to get user details for ID: " + userId);
    }

    /**
     * Cấm người dùng.
     */
    public void banUser(Long userId) {
        log.warn("Banning user with ID: {}", userId);
        ResponseEntity<ApiResponse<Void>> response = userServiceClient.banUser(userId);
        handleFeignResponse(response, "Failed to ban user: " + userId);
        log.info("Successfully banned user with ID: {}", userId);
    }

    /**
     * Bỏ cấm người dùng.
     */
    public void unbanUser(Long userId) {
        log.info("Unbanning user with ID: {}", userId);
        ResponseEntity<ApiResponse<Void>> response = userServiceClient.unbanUser(userId);
        handleFeignResponse(response, "Failed to unban user: " + userId);
        log.info("Successfully unbanned user with ID: {}", userId);
    }


    /**
     * Cảnh cáo người dùng (tăng warning count).
     */
    public void warnUser(Long userId) {
        log.warn("Warning user with ID: {}", userId);
        ResponseEntity<ApiResponse<Void>> response = userServiceClient.warnUser(userId);
        handleFeignResponse(response, "Failed to warn user: " + userId);
        log.info("Successfully warned user with ID: {}", userId);
    }


    public void changeRole(Long userId, UserRole role) {
        log.warn("Changing role for user ID: {} to role: {}", userId, role);
        ResponseEntity<ApiResponse<Void>> response=userServiceClient.changeRole(userId, role);
        handleFeignResponse(response, "Failed to change role for user ID: " + userId);
        log.info("Successfully changed role for user ID: {} to role: {}", userId, role);
    }

    /**
     * Lấy thống kê người dùng.
     */
    public UserStatsDTO getUserStats() {
        log.info("Getting user statistics");
        ResponseEntity<ApiResponse<UserStatsDTO>> response = userServiceClient.getUserStats();
        return handleFeignResponse(response, "Failed to get user stats");
    }



    /**
     * Hàm tiện ích xử lý response từ Feign Client.
     * @param response ResponseEntity từ Feign client
     * @param errorMessage Thông báo lỗi nếu request thất bại
     * @return Dữ liệu từ response nếu thành công
     * @throws ResponseStatusException Nếu request thất bại hoặc không có dữ liệu
     */
    private <T> T handleFeignResponse(ResponseEntity<ApiResponse<T>> response, String errorMessage) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getData() != null) {
            return response.getBody().getData();
        } else {
            HttpStatus status = (HttpStatus) response.getStatusCode();
            String message = (response.getBody() != null && response.getBody().getMessage() != null)
                    ? response.getBody().getMessage()
                    : errorMessage;
            log.error("{} - Status: {}, Message: {}", errorMessage, status, message);
            // Ném lỗi để GlobalExceptionHandler có thể bắt và trả về response chuẩn
            throw new ResponseStatusException(status, message);
        }
    }
    // Overload cho trường hợp response không có data (Void)
    private void handleVoidFeignResponse(ResponseEntity<ApiResponse<Void>> response, String errorMessage) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            HttpStatus status = (HttpStatus) response.getStatusCode(); // Cast an toàn hơn
            String message = (response.getBody() != null && response.getBody().getMessage() != null)
                    ? response.getBody().getMessage()
                    : errorMessage;
            log.error("{} - Status: {}, Message: {}", errorMessage, status, message);
            throw new ResponseStatusException(status, message);
        }
        // Thành công thì không cần làm gì
    }
}