package com.smartvn.admin_service.config;

import com.smartvn.admin_service.client.OrderServiceFallback;
import com.smartvn.admin_service.client.ProductServiceFallback;
import com.smartvn.admin_service.client.UserServiceFallback;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
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
//                template.header("Content-Type", "application/json");
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

    @Bean
    public OrderServiceFallback orderServiceFallback() {
        return new OrderServiceFallback();
    }

    @Bean
    public ProductServiceFallback productServiceFallback() {
        return new ProductServiceFallback();
    }

    @Bean
    public UserServiceFallback userServiceFallback() {
        return new UserServiceFallback();
    }
}