package com.smartvn.user_service.controller;

import com.smartvn.user_service.dto.address.AddressDTO;
import com.smartvn.user_service.dto.internal.UserInfoDTO;
import com.smartvn.user_service.dto.response.ApiResponse;
import com.smartvn.user_service.dto.user.UserDTO;
import com.smartvn.user_service.dto.user.UserStatsDTO;
import com.smartvn.user_service.enums.UserRole;
import com.smartvn.user_service.model.Address;
import com.smartvn.user_service.model.User;
import com.smartvn.user_service.repository.AddressRepository;
import com.smartvn.user_service.repository.UserRepository;
import com.smartvn.user_service.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /**
     * ✅ Lấy thông tin user (cho reviews)
     * Được gọi bởi: ReviewService.getReviewDTO()
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDTO> getUserInfo(
            @PathVariable("userId") Long userId) {

        UserInfoDTO userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/admin/{userId}")
    public ResponseEntity<UserDTO> getUserDTO(
            @PathVariable("userId") Long userId) {

        UserDTO userDTO = userService.getUserById(userId);
        log.info("userDTO(user service): {}", userDTO);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/{userId}/addresses/{addressId}/validate")
    public ResponseEntity<Boolean> validateUserAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        boolean isValid=addressRepository.existsByIdAndUserId(addressId, userId);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        return ResponseEntity.ok(new AddressDTO(address));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "search", required = false)  String search,
            @RequestParam(value = "role", required = false)  String role,
            @RequestParam(value = "isBanned", required = false) Boolean isBanned
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.searchUsers(search, role, isBanned, pageable);
        Page<UserDTO> userDTOS = users.map(userService::convertUserToDto);

        return ResponseEntity.ok(ApiResponse.success(userDTOS, "Get success"));
    }

    /**
     * ✅ BAN USER
     */
    @PutMapping("/{userId}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable Long userId) {
        userService.banUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User banned"));
    }

    /**
     * ✅ UNBAN USER
     */
    @PutMapping("/{userId}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable Long userId) {
        userService.unbanUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User unbanned"));
    }

    /**
     * ✅ WARN USER (Tăng warning count)
     */
    @PutMapping("/{userId}/warn")
    public ResponseEntity<ApiResponse<Void>> warnUser(@PathVariable Long userId) {
        userService.incrementWarningCount(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Warning added"));
    }

    /**
     * ✅ LẤY THỐNG KÊ USERS
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsDTO>> getUserStats() {
        UserStatsDTO stats = userService.calculateUserStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Stats retrieved"));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> changeRoleUser(@PathVariable Long userId, @RequestParam UserRole role) {
        userService.changeRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success(null, "User role changed successfully."));
    }

    @GetMapping("/stats/new-this-month")
    public ResponseEntity<Long> getNewUsersThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        long count = userRepository.countByCreatedAtAfter(startOfMonth);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<UserInfoDTO>> getUsersByIds(
            @RequestBody List<Long> userIds) {

        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // ✅ FETCH 1 LẦN từ DB thay vì N queries
        List<User> users = userRepository.findAllById(userIds);

        List<UserInfoDTO> dtos = users.stream()
                .map(user -> new UserInfoDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getImageUrl(),
                        user.isActive(),
                        user.isBanned()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
