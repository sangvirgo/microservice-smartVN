package com.smartvn.product_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // ✅ 1. API Key (đã có)
                template.header("X-API-KEY", internalApiKey);

                // ✅ 2. THÊM ĐOẠN NÀY - Forward X-User-Id
                try {
                    ServletRequestAttributes attributes =
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                    if (attributes != null) {
                        String userId = attributes.getRequest().getHeader("X-User-Id");

                        if (userId != null && !userId.isEmpty()) {
                            template.header("X-User-Id", userId);
                            System.out.println("✅ Feign forwarding X-User-Id: " + userId);
                        } else {
                            System.out.println("ℹ️ No X-User-Id header - guest user");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to forward X-User-Id: " + e.getMessage());
                }
            }
        };
    }
}