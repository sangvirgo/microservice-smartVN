package com.webanhang.team_project.controller.seller;

import com.webanhang.team_project.dto.address.AddAddressRequest;
import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.dto.seller.ChangePasswordRequest;
import com.webanhang.team_project.dto.seller.SellerProfileDTO;
import com.webanhang.team_project.dto.seller.UpdateSellerProfileRequest;
import com.webanhang.team_project.model.Address;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.repository.AddressRepository;
import com.webanhang.team_project.repository.UserRepository;
import com.webanhang.team_project.service.image.CloudinaryService;
import com.webanhang.team_project.service.image.ImageService;
import com.webanhang.team_project.service.seller.ISellerProfileService;
import com.webanhang.team_project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/seller/profile")
public class SellerProfileController {

    private final ISellerProfileService sellerProfileService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ApiResponse> getSellerProfile(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        SellerProfileDTO profileDTO = sellerProfileService.getSellerProfile(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(profileDTO, "Lấy thông tin hồ sơ người bán thành công"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse> updateSellerProfile(
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateSellerProfileRequest request) {

        User seller = userService.findUserByJwt(jwt);
        SellerProfileDTO updatedProfile = sellerProfileService.updateSellerProfile(seller.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Cập nhật hồ sơ người bán thành công"));
    }

    @GetMapping("/shop")
    public ResponseEntity<ApiResponse> getShopInfo(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        var shopInfo = sellerProfileService.getShopInfo(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(shopInfo, "Lấy thông tin cửa hàng thành công"));
    }

    @PutMapping("/shop")
    public ResponseEntity<ApiResponse> updateShopInfo(
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateSellerProfileRequest.ShopInfo shopInfo) {

        User seller = userService.findUserByJwt(jwt);
        var updatedShopInfo = sellerProfileService.updateShopInfo(seller.getId(), shopInfo);
        return ResponseEntity.ok(ApiResponse.success(updatedShopInfo, "Cập nhật thông tin cửa hàng thành công"));
    }

    @GetMapping("/verification-status")
    public ResponseEntity<ApiResponse> getVerificationStatus(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        var status = sellerProfileService.getVerificationStatus(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(status, "Lấy trạng thái xác minh thành công"));
    }

    @PostMapping("/avatar/upload")
    public ResponseEntity<ApiResponse> uploadAvatar(
            @RequestHeader("Authorization") String jwt,
            @RequestParam("file") MultipartFile file) {
        try {
            User seller = userService.findUserByJwt(jwt);

            // Upload to cloudinary
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
            String imageUrl = (String) uploadResult.get("url");

            // Update user's imageUrl
            seller.setImageUrl(imageUrl);
            userRepository.save(seller);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(ApiResponse.success(response, "Upload avatar thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Upload avatar thất bại: " + e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestHeader("Authorization") String jwt,
            @RequestBody ChangePasswordRequest request) {
        try {
            User seller = userService.findUserByJwt(jwt);

            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), seller.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Mật khẩu hiện tại không đúng"));
            }

            // Validate new password
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Mật khẩu mới phải có ít nhất 6 ký tự"));
            }

            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Xác nhận mật khẩu không khớp"));
            }

            // Update password
            seller.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(seller);

            return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Đổi mật khẩu thất bại: " + e.getMessage()));
        }
    }
}
