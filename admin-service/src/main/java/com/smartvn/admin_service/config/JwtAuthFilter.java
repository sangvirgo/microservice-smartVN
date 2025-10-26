package com.smartvn.admin_service.config;

import com.smartvn.admin_service.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter chạy một lần cho mỗi request để xử lý JWT Authentication.
 * Đọc token từ header, xác thực, và thiết lập SecurityContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Parse JWT từ header Authorization
            String jwt = parseJwt(request);

            // 2. Nếu có token và token hợp lệ
            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
                // 3. Lấy thông tin từ token (email, roles, id)
                Claims claims = jwtUtils.getClaimsFromToken(jwt);
                String email = claims.getSubject();
                // Roles được lưu dưới dạng List<String> trong claim 'roles'
                @SuppressWarnings("unchecked") // JWT library trả về List, cần cast
                List<String> roles = claims.get("roles", List.class);

                // 4. Tạo danh sách GrantedAuthority từ roles
                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                // 5. Tạo đối tượng Authentication
                // Principal có thể là email hoặc một đối tượng UserDetails đơn giản nếu cần
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, // Principal là email
                        null,   // Credentials là null vì dùng token
                        authorities); // Danh sách quyền

                // 6. Set chi tiết xác thực (IP, session - không quan trọng lắm với stateless)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Lưu Authentication vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("User '{}' authenticated with roles: {}", email, roles);
            }
        } catch (Exception e) {
            // Log lỗi nếu có vấn đề khi xử lý token
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Không ném lỗi ra ngoài, để request tiếp tục và bị chặn bởi security config nếu cần
        }

        // 8. Chuyển request/response cho filter tiếp theo trong chuỗi
        filterChain.doFilter(request, response);
    }

    /**
     * Trích xuất JWT từ header "Authorization: Bearer <token>".
     * @param request HttpServletRequest
     * @return JWT string hoặc null nếu không có.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Bỏ "Bearer "
        }

        return null; // Không tìm thấy token hợp lệ
    }
}