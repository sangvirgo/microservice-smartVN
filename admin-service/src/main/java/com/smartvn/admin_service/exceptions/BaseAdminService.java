package com.smartvn.admin_service.exceptions;

import com.smartvn.admin_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

// ✅ Tạo Base Service để tái sử dụng logic chung
@Slf4j
public abstract class BaseAdminService {

    protected <T> T handleFeignResponse(
            ResponseEntity<ApiResponse<T>> response,
            String errorMessage) {

        if (response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null
                && response.getBody().getData() != null) {
            return response.getBody().getData();
        }

        HttpStatus status = (HttpStatus) response.getStatusCode();
        String message = Optional.ofNullable(response.getBody())
                .map(ApiResponse::getMessage)
                .orElse(errorMessage);

        log.error("{} - Status: {}, Message: {}", errorMessage, status, message);
        throw new AppException(message, status);
    }

    protected void handleVoidResponse(
            ResponseEntity<ApiResponse<Void>> response,
            String errorMessage) {

        if (!response.getStatusCode().is2xxSuccessful()) {
            HttpStatus status = (HttpStatus) response.getStatusCode();
            String message = Optional.ofNullable(response.getBody())
                    .map(ApiResponse::getMessage)
                    .orElse(errorMessage);

            log.error("{} - Status: {}, Message: {}", errorMessage, status, message);
            throw new AppException(message, status);
        }
    }
}