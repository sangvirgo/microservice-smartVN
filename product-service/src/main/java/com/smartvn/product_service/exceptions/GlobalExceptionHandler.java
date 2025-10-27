package com.smartvn.product_service.exceptions;

import com.smartvn.product_service.dto.response.ApiResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException; // Thêm import này nếu cần
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Thêm nếu dùng Spring Security
import org.springframework.security.authentication.BadCredentialsException; // Thêm nếu dùng Spring Security
import org.springframework.security.authentication.DisabledException; // Thêm nếu dùng Spring Security
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// import javax.naming.AuthenticationException; // Import này có thể không cần nếu dùng Spring Security exceptions
import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý tập trung các exception xảy ra trong ứng dụng
 * và trả về định dạng ApiResponse chuẩn.
 */
@ControllerAdvice
@Slf4j // Thêm annotation Slf4j để sử dụng logger
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFoundException(EntityNotFoundException ex){
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                // Sử dụng factory method chuẩn của ApiResponse
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND"));
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityExistsException(EntityExistsException ex){
        log.warn("Entity exists conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT, "ENTITY_EXISTS"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex){
        log.error("Database integrity violation: {}", ex.getMessage(), ex); // Log cả stack trace
        // Trả về thông báo chung chung hơn cho client
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Database Error: A data integrity issue occurred. Check server logs.", HttpStatus.CONFLICT, "DB_INTEGRITY_VIOLATION"));
    }

    /* // Bỏ comment nếu bạn dùng AuthenticationException cũ hoặc custom
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex){
         log.warn("Authentication error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication Error: " + ex.getMessage(), HttpStatus.UNAUTHORIZED, "AUTH_ERROR"));
    }*/

    @ExceptionHandler(BadCredentialsException.class) // Dùng cho Spring Security
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex){
        log.warn("Invalid credentials attempted.");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid credentials.", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));
    }

    @ExceptionHandler(DisabledException.class) // Dùng cho Spring Security
    public ResponseEntity<ApiResponse<Object>> handleDisabledException(DisabledException ex){
        log.warn("Attempt to access with disabled account: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Account is disabled.", HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED"));
    }

    @ExceptionHandler(AccessDeniedException.class) // Dùng cho Spring Security @PreAuthorize etc.
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access Denied: You do not have permission to access this resource.", HttpStatus.FORBIDDEN, "ACCESS_DENIED"));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) { // Trả về Map lỗi
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        // Tạo response lỗi với Map errors trong data
        ApiResponse<Map<String, String>> errorResponse = ApiResponse.error("Validation Error", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        errorResponse.setData(errors); // Đặt Map lỗi vào trường data
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class) // Lỗi validation khác (vd: @PathVariable, @RequestParam)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation Error: " + ex.getMessage(), HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex){
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid Argument: " + ex.getMessage(), HttpStatus.BAD_REQUEST, "ILLEGAL_ARGUMENT"));
    }

    // --- Handler cho AppException ---
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        HttpStatus status = ex.getStatus();
        // Log lỗi dựa trên status code
        if (status.is5xxServerError()) {
            log.error("Application error [{}] ({}): {}", status.value(), status.getReasonPhrase(), ex.getMessage(), ex); // Log cả stack trace nếu có
        } else {
            log.warn("Application warning [{}] ({}): {}", status.value(), status.getReasonPhrase(), ex.getMessage());
        }
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage(), status, "APP_ERROR_" + status.value()));
    }

    // --- Handler CỰC KỲ QUAN TRỌNG cho NullPointerException ---
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(NullPointerException ex) {
        // !!! Log lỗi gốc với đầy đủ stack trace !!!
        log.error("!!! Critical: Caught NullPointerException: {}", ex.getMessage(), ex);
        // Trả về lỗi 500 với thông báo rõ ràng hơn (nhưng không lộ chi tiết lỗi cho client)
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected internal error occurred (NPE). Please check server logs for details.", HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_NULL_POINTER"));
    }

    // --- Handler bắt các Exception chung khác (cuối cùng) ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex){
        // !!! Log lỗi gốc với đầy đủ stack trace !!!
        log.error("!!! Critical: Caught unhandled Exception ({}): {}", ex.getClass().getName(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected general error occurred. Please check server logs.", HttpStatus.INTERNAL_SERVER_ERROR, "UNHANDLED_EXCEPTION"));
    }
}