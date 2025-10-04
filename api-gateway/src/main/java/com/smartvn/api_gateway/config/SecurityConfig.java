//package com.smartvn.api_gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        // Public endpoints - không cần authentication
//                        .requestMatchers(
//                                "/api/v1/auth/**",           // Login, Register
//                                "/api/v1/products/**",       // Xem sản phẩm
//                                "/api/v1/categories/**",     // Xem danh mục
//                                "/actuator/health"           // Health check
//                        ).permitAll()
//
//                        // User endpoints - cần authentication
//                        .requestMatchers(
//                                "/api/v1/users/**",          // User profile
//                                "/api/v1/cart/**",           // Giỏ hàng
//                                "/api/v1/orders/**"          // Đơn hàng
//                        ).authenticated()
//
//                        // Admin endpoints - cần ADMIN role
//                        .requestMatchers("/api/v1/admin/**")
//                        .hasRole("ADMIN")
//
//                        .anyRequest().authenticated()
//                )
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(management -> management
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                );
//        return http.build();
//    }
//}