package com.smartvn.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp response chuẩn cho tất cả các API của Admin Service.
 * @param <T> Kiểu dữ liệu của payload (data).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private T data;              // Dữ liệu trả về (có thể là object, list, page,...)
    private String message;      // Thông báo mô tả thành công hoặc lỗi
    private Map<String, Object> pagination;  // Thông tin phân trang (nếu data là Page)
    private String error;        // Mã lỗi hoặc loại lỗi (cho trường hợp lỗi)
    private Integer status;      // Mã trạng thái HTTP (thường dùng cho lỗi)

    /**
     * Tạo response thành công với dữ liệu và thông báo.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .data(data)
                .message(message)
                .status(HttpStatus.OK.value()) // Mặc định là 200 OK
                .build();
    }

    /**
     * Tạo response thành công với dữ liệu dạng Page (tự động thêm thông tin pagination).
     */
    public static <T> ApiResponse<List<T>> success(Page<T> pageData, String message) {
        Map<String, Object> paginationInfo = new HashMap<>();
        paginationInfo.put("currentPage", pageData.getNumber());
        paginationInfo.put("totalPages", pageData.getTotalPages());
        paginationInfo.put("totalItems", pageData.getTotalElements());
        paginationInfo.put("pageSize", pageData.getSize());
        paginationInfo.put("isFirst", pageData.isFirst());
        paginationInfo.put("isLast", pageData.isLast());

        return ApiResponse.<List<T>>builder()
                .data(pageData.getContent()) // Chỉ lấy nội dung của trang
                .message(message)
                .pagination(paginationInfo) // Thêm thông tin phân trang
                .status(HttpStatus.OK.value())
                .build();
    }

    /**
     * Tạo response lỗi với thông báo và mã trạng thái HTTP.
     */
    public static <T> ApiResponse<T> error(String message, HttpStatus status, String errorCode) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(status.value())
                .error(errorCode) // Mã lỗi tùy chỉnh
                .build();
    }

    /**
     * Tạo response lỗi với thông báo (mặc định 500 Internal Server Error).
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }
}