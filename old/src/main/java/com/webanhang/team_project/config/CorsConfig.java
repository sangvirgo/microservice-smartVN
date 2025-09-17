package com.webanhang.team_project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${api.prefix}")
    private String API;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Danh sách các pattern của origin được phép
        String[] allowedOriginPatterns = new String[]{
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5175",
            "https://*.vercel.app" // Chấp nhận tất cả các tên miền phụ của vercel.app
        };

        registry.addMapping(API + "/**")
                // Sử dụng allowedOriginPatterns thay cho allowedOrigins
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Thêm PATCH nếu cần
                .allowedHeaders("*")
                .exposedHeaders("CF-IPCountry", "CF-RAY", "CF-Connecting-IP")
                .allowCredentials(true)
                .maxAge(3600);
    }
}