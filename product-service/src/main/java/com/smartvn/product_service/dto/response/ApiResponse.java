package com.smartvn.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}