package com.smartvn.user_service.controller;

import com.smartvn.user_service.dto.address.AddAddressRequest;
import com.smartvn.user_service.dto.address.AddressDTO;
import com.smartvn.user_service.dto.response.ApiResponse;
import com.smartvn.user_service.dto.user.UpdateUserRequest;
import com.smartvn.user_service.dto.user.UserDTO;
import com.smartvn.user_service.model.Address;
import com.smartvn.user_service.model.User;
import com.smartvn.user_service.repository.UserRepository;
import com.smartvn.user_service.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getUserProfile(@RequestHeader("Authorization") String jwt) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
            }
            UserDTO userProfile = userService.findUserProfileByJwt(jwt);
            if(userProfile.isActive()==false) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Forbidden"));
            }
            return ResponseEntity.ok(ApiResponse.success(userProfile, "User Profile"));
        } catch (Exception e) {
            log.error("Error getting user profile: ", e);
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PutMapping("/profile/update")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserProfile(
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateUserRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }
        User currentUser = userService.findUserByJwt(jwt);
        UserDTO updatedUser = userService.updateUser(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User profile updated successfully."));
    }

    @PostMapping("/addresses")
    @Transactional
    public ResponseEntity<?> addUserAddress(@RequestHeader("Authorization") String jwt,
                                            @RequestBody AddAddressRequest req) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed", "code", "AUTH_ERROR"));
        }
        User user  = userService.findUserByJwt(jwt);
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Email chưa được đăng ký hoặc không hợp lệ."));
        }
        userService.addUserAddress(user.getId(), req);
        return ResponseEntity.ok(Map.of("message", "Address added successfully"));
    }

    @Transactional
    @GetMapping("/address")
    public ResponseEntity<?> getUserAddress(@RequestHeader("Authorization") String jwt) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication failed", "code", "AUTH_ERROR"));
            }

            User user = userService.findUserByJwt(jwt);

            if(user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found for email: " + user.getEmail(), "code", "USER_NOT_FOUND"));
            }

            List<AddressDTO> addressDTOS = new ArrayList<>();

            if(user.getAddresses() != null) {
                for (Address a: user.getAddresses()) {
                    if (a.getIsActive()) {
                        addressDTOS.add(new AddressDTO(a));
                    }
                }
            }

            return ResponseEntity.ok(addressDTOS);
        } catch (Exception e) {
            log.error("Error getting user address: ", e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage(), "code", "INTERNAL_ERROR"));
        }
    }


    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long addressId) {

        User user = userService.findUserByJwt(jwt);
        userService.softDeleteAddress(user.getId(), addressId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Địa chỉ đã được xóa thành công")
        );
    }
}