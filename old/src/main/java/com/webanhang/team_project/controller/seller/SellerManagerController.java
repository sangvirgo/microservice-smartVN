package com.webanhang.team_project.controller.seller;

import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.dto.seller.SellerRoleDTO;
import com.webanhang.team_project.dto.seller.SellerStatusDTO;
import com.webanhang.team_project.enums.UserRole;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller quản lý các endpoint chung cho người bán
 * Bao gồm các chức năng xác thực vai trò, kiểm tra quyền,...
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/seller")
public class SellerManagerController {

    private final UserService userService;

    @GetMapping("/verify-role")
    public ResponseEntity<ApiResponse> verifySellerRole(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        boolean isSeller = user.getRole() != null && user.getRole().getName() == UserRole.SELLER;

        SellerRoleDTO roleDTO = new SellerRoleDTO(isSeller);
        String message = isSeller ? "Người dùng có vai trò SELLER" : "Người dùng không có vai trò SELLER";

        return ResponseEntity.ok(ApiResponse.success(roleDTO, message));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getSellerStatus(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        if (user.getRole() == null || user.getRole().getName() != UserRole.SELLER) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Người dùng không có vai trò SELLER"));
        }
        SellerStatusDTO statusDTO = new SellerStatusDTO(
                user.isActive(),
                user.isActive() ? "ACTIVE" : "INACTIVE"
        );
        String message = user.isActive()
                ? "Tài khoản người bán đang hoạt động"
                : "Tài khoản người bán chưa được kích hoạt";

        return ResponseEntity.ok(ApiResponse.success(statusDTO, message));
    }
}
