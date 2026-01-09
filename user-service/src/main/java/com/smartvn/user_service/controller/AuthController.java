package com.smartvn.user_service.controller;

import com.smartvn.user_service.dto.auth.ForgotPasswordRequest;
import com.smartvn.user_service.dto.auth.LoginRequest;
import com.smartvn.user_service.dto.auth.OtpVerificationRequest;
import com.smartvn.user_service.dto.auth.RegisterRequest;
import com.smartvn.user_service.dto.response.ApiResponse;
import com.smartvn.user_service.dto.user.UserDTO;
import com.smartvn.user_service.model.User;
import com.smartvn.user_service.repository.UserRepository;
import com.smartvn.user_service.security.jwt.JwtUtils;
import com.smartvn.user_service.service.otp.OtpService;
import com.smartvn.user_service.service.userdetails.AppUserDetails;
import com.smartvn.user_service.service.userdetails.AppUserDetailsService;
import com.smartvn.user_service.service.user.UserService;
import com.smartvn.user_service.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final JwtUtils jwtUtils;
    private final CookieUtils cookieUtils;
    private final AppUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final OtpService otpService;

    @Value("${auth.token.refreshExpirationInMils}")
    private Long refreshTokenExpirationTime;

// Trong file AuthController.java
// Thay thế phương thức cũ bằng phương thức này

// Trong file AuthController.java
// Thay thế phương thức cũ bằng phương thức này

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticateUser(@RequestBody LoginRequest request,
                                                        HttpServletResponse response) {
        try {
            // Kiểm tra user tồn tại và trạng thái TRƯỚC KHI authenticate
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

            if (userOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Email hoặc mật khẩu không đúng!"));
            }

            User user = userOptional.get();

            // Kiểm tra tài khoản có bị ban không
            if (user.isBanned()) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Tài khoản của bạn đã bị khóa."));
            }

            // Kiểm tra tài khoản đã được kích hoạt chưa
            if (!user.isActive()) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Tài khoản của bạn chưa được kích hoạt. Vui lòng kiểm tra email để xác thực."));
            }

            // Tiến hành authenticate nếu mọi thứ OK
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            // Tạo tokens
            String accessToken = jwtUtils.generateAccessToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(request.getEmail());
            cookieUtils.addRefreshTokenCookie(response, refreshToken, refreshTokenExpirationTime);

            // Chuẩn bị response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("email", user.getEmail());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName", user.getLastName());
            userMap.put("role", user.getRole().getName().name());
            userMap.put("isActive", user.isActive());
            userMap.put("isBanned", user.isBanned());
            responseData.put("user", userMap);

            return ResponseEntity.ok(ApiResponse.success(responseData, "Đăng nhập thành công"));

        } catch (AuthenticationException e) {
            // Chỉ bắt lỗi sai mật khẩu
            log.error("Authentication failed for email: {}", request.getEmail(), e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Email hoặc mật khẩu không đúng!"));
        } catch (Exception e) {
            log.error("Unexpected error during login: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Mã xác thực đã được gửi tới email. Vui lòng kiểm tra và xác thực."));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody OtpVerificationRequest request) {
        try {
            boolean isVerified = userService.verifyOtp(request);
            if (isVerified) {
                return ResponseEntity.ok(ApiResponse.success(null, "Xác thực thành công! Tài khoản đã được kích hoạt."));
            }
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Mã OTP không hợp lệ hoặc đã hết hạn."));
        } catch (Exception e) {
            log.error("Lỗi xác thực OTP: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi xác thực OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse> refreshAccessToken(HttpServletRequest request) {
        try {
            String refreshToken = cookieUtils.getRefreshTokenFromCookies(request);
            if (refreshToken != null && jwtUtils.validateToken(refreshToken)) {
                String usernameFromToken = jwtUtils.getEmailFromToken(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(usernameFromToken);
                String newAccessToken = jwtUtils.generateAccessToken(
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

                Map<String, String> token = new HashMap<>();
                token.put("accessToken", newAccessToken);
                return ResponseEntity.ok(ApiResponse.success(token, "Access token mới đã được tạo."));
            }
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Refresh token không hợp lệ hoặc đã hết hạn."));
        } catch (Exception e) {
            log.error("Lỗi refresh token: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi refresh token: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
        cookieUtils.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.success(null, "Đăng xuất thành công!"));
    }

    @PostMapping("/register/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email không được để trống."));
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Email chưa được đăng ký hoặc không hợp lệ."));
        }
        if (!otpService.isResendAllowed(email)) {
            long remainingSeconds = otpService.getRemainingCooldownSeconds(email);
            String waitMessage = String.format("Vui lòng đợi %d giây trước khi yêu cầu mã OTP mới.", remainingSeconds);
            // Use TOO_MANY_REQUESTS (429) for rate limiting is more appropriate
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS) // Use 429 status code
                    .body(ApiResponse.error(waitMessage));
        }

        try {
            String otp = otpService.generateOtp(email); // This now also updates the generation time
            otpService.sendOtpEmail(email, otp);
            return ResponseEntity.ok(ApiResponse.success(null, "Mã OTP mới đã được gửi tới email. Vui lòng kiểm tra hộp thư của bạn."));
        } catch (Exception e) {
            log.error("Lỗi khi gửi lại OTP cho email {}: ", email, e); // Log email for context
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi khi gửi OTP. Vui lòng thử lại sau.")); // More generic error message
        }
    }


    @PostMapping("/register/forgot-password")
    public ResponseEntity<ApiResponse> forgotPass(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        try {
            OtpVerificationRequest tmp =new OtpVerificationRequest();
            tmp.setEmail(forgotPasswordRequest.getEmail());
            tmp.setOtp(forgotPasswordRequest.getOtp());

            boolean isVerified = userService.verifyOtp(tmp);

            if (isVerified) {
                userService.forgotPassword(forgotPasswordRequest.getEmail(), forgotPasswordRequest.getNewPassword());
                return ResponseEntity.ok(ApiResponse.success(null, "Mật khẩu đã được thay đổi thành công!"));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Mã OTP không hợp lệ hoặc đã hết hạn."));
            }
        } catch (Exception e) {
            log.error("Lỗi xác thực OTP: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi xác thực OTP: " + e.getMessage()));
        }
    }
}