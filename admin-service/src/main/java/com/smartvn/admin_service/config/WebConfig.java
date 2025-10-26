package com.smartvn.admin_service.config;

import com.smartvn.admin_service.enums.UserRole;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToUserRoleConverter());
    }

    // ✅ INNER CLASS - Không cần file riêng
    public static class StringToUserRoleConverter implements Converter<String, UserRole> {
        @Override
        public UserRole convert(String source) {
            try {
                return UserRole.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + source);
            }
        }
    }
}