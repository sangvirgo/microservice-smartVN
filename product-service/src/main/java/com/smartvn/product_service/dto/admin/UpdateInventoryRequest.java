package com.smartvn.product_service.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateInventoryRequest {
    // Có thể dùng cho cả add và update
    @Size(max = 50, message = "Size tối đa 50 ký tự")
    private String size; // Bắt buộc khi add, có thể null khi update nếu không đổi size

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @Min(value = 0, message = "Giảm giá phải từ 0")
    @Max(value = 100, message = "Giảm giá tối đa 100")
    private Integer discountPercent = 0; // Mặc định là 0
}