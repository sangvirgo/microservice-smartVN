package com.smartvn.admin_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtEntryPoint authEntryPoint; // Xử lý lỗi khi authentication thất bại
    private final JwtAuthFilter jwtAuthFilter;   // Filter để đọc JWT và thiết lập Authentication context
    private final AccessDeniedHandler accessDeniedHandler; // Xử lý lỗi khi user không có quyền truy cập
    private final ApiKeyAuthFilter apiKeyAuthFilter; // ✅ Inject ApiKeyAuthFilter

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Vô hiệu hóa CSRF vì dùng JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // Cấu hình xử lý exception
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint) // Lỗi xác thực
                        .accessDeniedHandler(accessDeniedHandler)    // Lỗi phân quyền
                )
                // Không tạo session, vì là stateless API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Cấu hình phân quyền cho các request
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập các endpoint public (health check, swagger)
                        .requestMatchers(
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/users/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/users/{id}/ban").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/users/{id}/warn").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/users/{id}/unban").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/products", "/api/v1/admin/products/bulk").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/products/{id}/images").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/products/images/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/products/*/inventory").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/products/{id}/inventory/**").hasAnyRole("ADMIN", "STAFF")

                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/products/**").hasAnyRole("ADMIN", "STAFF")

                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/orders/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/orders/{id}").hasAnyRole("ADMIN", "STAFF") // Cho STAFF update
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/orders/stats").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/reviews/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/reviews/**").hasAnyRole("ADMIN", "STAFF")

                        .requestMatchers("/api/v1/admin/dashboard/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                // Thêm filter JWT vào trước filter mặc định
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}