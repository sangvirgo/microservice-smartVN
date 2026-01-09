package com.smartvn.admin_service.exceptions;

import com.smartvn.admin_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý tập trung các exception xảy ra trong ứng dụng
 * và trả về định dạng ApiResponse chuẩn.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Bắt các lỗi ResponseStatusException (thường được ném từ service layer khi gọi Feign thất bại).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String message = ex.getReason() != null ? ex.getReason() : "An error occurred";
        log.error("ResponseStatusException: Status {}, Reason: {}", status, message, ex);
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(message, HttpStatus.valueOf(status.value()), "SERVICE_COMMUNICATION_ERROR"));
    }

    /**
     * Bắt lỗi AccessDeniedException từ @PreAuthorize.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Bạn không có quyền thực hiện hành động này.", HttpStatus.FORBIDDEN, "ACCESS_DENIED"));
    }

    /**
     * Bắt lỗi validation từ @Valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        log.warn("Validation failed: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.error("Dữ liệu không hợp lệ.", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        response.setData(errors); // Đặt chi tiết lỗi validation vào data
        return ResponseEntity.badRequest().body(response);
    }


    /**
     * Bắt các lỗi chung khác.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR"));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        log.error("Application error: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage(), ex.getStatus(), "APP_ERROR"));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getAllValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();

            result.getResolvableErrors().forEach(error -> {
                String msg = error.getDefaultMessage();
                errors.put(paramName != null ? paramName : "field", msg);
            });
        });

        log.error("Validation failed: {}", errors);

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(
                        "Validation failed: " + errors,
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_ERROR"
                ));
    }
}