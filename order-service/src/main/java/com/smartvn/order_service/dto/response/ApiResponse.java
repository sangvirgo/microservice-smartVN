package com.smartvn.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private T data;              // Dữ liệu trả về
    private String message;      // Thông báo mô tả
    private Map<String, Object> pagination;  // Thông tin phân trang (nếu có)

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> pagination) {
        return new ApiResponse<>(data, message, pagination);
    }
}