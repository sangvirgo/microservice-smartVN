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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
//@RequestMapping("${api.prefix}/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticateUser(@RequestBody LoginRequest request,
            HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            String accessToken = jwtUtils.generateAccessToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(request.getEmail());
            cookieUtils.addRefreshTokenCookie(response, refreshToken, refreshTokenExpirationTime);

            AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).orElseThrow();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("email", user.getEmail());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName", user.getLastName());
            userMap.put("role", user.getRole().getName().name());
            userMap.put("isActive", user.isActive());
            responseData.put("user", userMap);

            return ResponseEntity.ok(ApiResponse.success(responseData, "Đăng nhập thành công"));
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Email hoặc mật khẩu không đúng!"));
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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        if (user == null) {
            // Security consideration: Avoid confirming if an email is registered or not here.
            // You might want to return a generic success message even if the email doesn't exist
            // to prevent email enumeration attacks. However, for user experience,
            // the current "Not Found" is clearer during development/testing.
            // Let's keep the "Not Found" for now, but be aware of this.
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Email chưa được đăng ký hoặc không hợp lệ."));
        }

        // --- Add cooldown check ---
        if (!otpService.isResendAllowed(email)) {
            long remainingSeconds = otpService.getRemainingCooldownSeconds(email);
            String waitMessage = String.format("Vui lòng đợi %d giây trước khi yêu cầu mã OTP mới.", remainingSeconds);
            // Use TOO_MANY_REQUESTS (429) for rate limiting is more appropriate
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS) // Use 429 status code
                    .body(ApiResponse.error(waitMessage));
        }

        // --- Proceed if allowed ---
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