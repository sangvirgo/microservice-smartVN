package com.smartvn.admin_service.config;

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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ✅ CHỈ xử lý /internal/** paths
        if (path.startsWith("/api/v1/internal/")) {
            String requestApiKey = request.getHeader(API_KEY_HEADER);

            if (!secretApiKey.equals(requestApiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid or missing API Key\"}");
                return; // ❌ DỪNG chain nếu sai key
            }
            // ✅ Key hợp lệ → Tiếp tục
        }

        // ✅ Cho TẤT CẢ requests đi tiếp (kể cả non-internal)
        filterChain.doFilter(request, response);
    }
}