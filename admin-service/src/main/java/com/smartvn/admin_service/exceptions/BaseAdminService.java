package com.smartvn.admin_service.exceptions;

import com.smartvn.admin_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;
// BaseAdminService.java - Improved Version
@Slf4j
public abstract class BaseAdminService {

    // ✅ GENERIC METHOD - Xử lý mọi response có data
    protected <T> T handleResponse(
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

    // ✅ VOID METHOD - Xử lý response không có data
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

    // ✅ PAGE METHOD - Xử lý Page response
    protected <T> Page<T> handlePageResponse(
            ResponseEntity<ApiResponse<Page<T>>> response,
            String errorMessage) {
        return handleResponse(response, errorMessage);
    }
}