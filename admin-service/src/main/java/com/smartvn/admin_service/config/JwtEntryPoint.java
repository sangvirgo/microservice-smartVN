package com.smartvn.admin_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý các lỗi AuthenticationException (ví dụ: token không hợp lệ, thiếu token).
 * Trả về lỗi 401 Unauthorized.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper; // Dùng để ghi JSON vào response

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Tạo body response lỗi dạng JSON
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", "Yêu cầu xác thực không hợp lệ hoặc thiếu thông tin.");
        body.put("path", request.getServletPath());

        // Ghi JSON vào output stream của response
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}