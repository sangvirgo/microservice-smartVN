package com.smartvn.order_service.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Custom Error Decoder cho Feign Client
 * Xử lý các lỗi từ các service khác
 */
@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign client error. Method: {}, Status: {}", methodKey, response.status());

        HttpStatus status = HttpStatus.valueOf(response.status());
        String message = String.format("Error calling %s: %s", methodKey, status.getReasonPhrase());

        switch (status) {
            case NOT_FOUND:
                return new RuntimeException("Resource not found: " + methodKey);
            case BAD_REQUEST:
                return new RuntimeException("Bad request: " + methodKey);
            case UNAUTHORIZED:
            case FORBIDDEN:
                return new RuntimeException("Authentication/Authorization failed: " + methodKey);
            case INTERNAL_SERVER_ERROR:
                return new RuntimeException("Internal server error from: " + methodKey);
            case SERVICE_UNAVAILABLE:
                return new RuntimeException("Service unavailable: " + methodKey);
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}