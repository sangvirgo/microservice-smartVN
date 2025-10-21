package com.smartvn.order_service.service;


import com.smartvn.order_service.client.UserServiceClient;
import com.smartvn.order_service.dto.user.UserDTO;
import com.smartvn.order_service.exceptions.AppException;
import com.smartvn.order_service.util.JwtUtils;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final JwtUtils  jwtUtils;
    private final UserServiceClient userServiceClient;

    public Long getUserIdFromJwt(String jwt) {
        if(jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        if(!jwtUtils.validateToken(jwt)) {
            throw new AppException("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        return jwtUtils.getUserIdFromToken(jwt);
    }

    /**
     * Validate user có tồn tại và active không
     * Gọi qua Feign Client
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "validateUserFallback")
    public void validateUser(Long userId) {
        try {
            UserDTO user = userServiceClient.getUserById(userId);

            if (user == null) {
                throw new AppException("User not found", HttpStatus.NOT_FOUND);
            }

            if (user.isBanned()) {
                throw new AppException("User is banned", HttpStatus.FORBIDDEN);
            }

            if (!user.isActive()) {
                throw new AppException("User account is not active", HttpStatus.FORBIDDEN);
            }

        } catch (FeignException.NotFound e) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }
    }

    private void validateUserFallback(Long userId, Exception e) {
        log.error("Failed to validate user {} after retries: {}", userId, e.getMessage());
        throw new AppException(
                "User service temporarily unavailable",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
