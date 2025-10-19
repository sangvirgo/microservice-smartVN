package com.smartvn.user_service.controller;

import com.smartvn.user_service.dto.address.AddressDTO;
import com.smartvn.user_service.dto.internal.UserInfoDTO;
import com.smartvn.user_service.model.Address;
import com.smartvn.user_service.repository.AddressRepository;
import com.smartvn.user_service.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final AddressRepository addressRepository;

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
}
