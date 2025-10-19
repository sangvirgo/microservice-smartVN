package com.smartvn.order_service.config;

import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("X-API-KEY", internalApiKey);
            }
        };
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                5000,  // connectTimeout (ms)
                10000  // readTimeout (ms)
        );
    }
}