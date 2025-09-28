package com.smartvn.user_service.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartvn.user_service.utils.ErrorResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtEntryPoint implements AuthenticationEntryPoint {
    private final ErrorResponseUtils errorResponseUtils;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        errorResponseUtils.sendAuthenticationError(response,
                "Thông tin xác thực không hợp lệ. Vui lòng đăng nhập lại.");
    }
}
