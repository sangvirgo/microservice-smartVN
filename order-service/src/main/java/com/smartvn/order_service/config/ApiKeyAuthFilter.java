package com.smartvn.order_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${internal.api.key}")
    private String secretApiKey;

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Chỉ áp dụng filter này cho các đường dẫn internal
        if (request.getServletPath().startsWith("/api/v1/internal/")) {
            String requestApiKey = request.getHeader(API_KEY_HEADER);

            if (secretApiKey.equals(requestApiKey)) {
                // Key hợp lệ, cho phép request đi tiếp
                filterChain.doFilter(request, response);
            } else {
                // Key không hợp lệ, trả về lỗi 401 Unauthorized
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid API Key");
                return; // Dừng chuỗi filter
            }
        } else {
            // Nếu không phải đường dẫn internal, bỏ qua và đi tiếp
            filterChain.doFilter(request, response);
        }
    }
}