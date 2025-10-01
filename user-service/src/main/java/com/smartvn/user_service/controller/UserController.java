package com.smartvn.user_service.controller;

import com.smartvn.user_service.dto.address.AddAddressRequest;
import com.smartvn.user_service.dto.address.AddressDTO;
import com.smartvn.user_service.dto.response.ApiResponse;
import com.smartvn.user_service.dto.user.UpdateUserRequest;
import com.smartvn.user_service.dto.user.UserDTO;
import com.smartvn.user_service.model.User;
import com.smartvn.user_service.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getUserProfile(@RequestHeader("Authorization") String jwt) {
        UserDTO userProfile = userService.findUserProfileByJwt(jwt);
        return ResponseEntity.ok(ApiResponse.success(userProfile, "User profile retrieved successfully."));
    }

    @PutMapping("/profile/update")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserProfile(
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateUserRequest request) {
        User currentUser = userService.findUserByJwt(jwt);
        UserDTO updatedUser = userService.updateUser(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User profile updated successfully."));
    }

    @PostMapping("/addresses/add")
    public ResponseEntity<ApiResponse<AddressDTO>> addUserAddress(
            @RequestHeader("Authorization") String jwt,
            @RequestBody AddAddressRequest request) {
        User currentUser = userService.findUserByJwt(jwt);
        AddressDTO newAddress = userService.addUserAddress(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(newAddress, "Address added successfully."));
    }
}