package com.smartvn.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private T data;              // Dữ liệu trả về
    private String message;      // Thông báo mô tả
    private Map<String, Object> pagination;  // Thông tin phân trang (nếu có)

    // Constructor cơ bản cho phản hồi thành công
    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    // Constructor cho phản hồi thành công có phân trang
    public ApiResponse(T data, String message, Map<String, Object> pagination) {
        this.data = data;
        this.message = message;
        this.pagination = pagination;
    }

    // Constructor cho phản hồi lỗi
    public ApiResponse(String message) {
        this.message = message;
        this.data = null;
    }


    // Factory methods
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> pagination) {
        return new ApiResponse<>(data, message, pagination);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message);
    }
}
