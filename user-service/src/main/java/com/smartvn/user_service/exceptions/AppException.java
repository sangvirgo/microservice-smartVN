package com.smartvn.user_service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    /**
     * Constructor chính để tạo một exception với thông điệp và mã trạng thái HTTP.
     *
     * @param message Thông điệp lỗi, sẽ được trả về cho client.
     * @param status  Mã trạng thái HTTP tương ứng với lỗi (ví dụ: NOT_FOUND, BAD_REQUEST).
     */
    public AppException(String message, HttpStatus status) {
        super(message); // Gọi constructor của lớp cha để lưu lại message
        this.message = message;
        this.status = status;
    }

    /**
     * Constructor phụ, sử dụng khi có một exception gốc (cause).
     *
     * @param message Thông điệp lỗi.
     * @param status  Mã trạng thái HTTP.
     * @param cause   Exception gốc gây ra lỗi này.
     */
    public AppException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.status = status;
    }
}