package com.smartvn.order_service.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Address từ User Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;
    private String fullName;
    private String province;
    private String ward;
    private String street;
    private String note;
    private String phoneNumber;
    private Long userId;

    /**
     * Helper: Lấy địa chỉ đầy đủ
     */
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();

        if (street != null) address.append(street).append(", ");
        if (ward != null) address.append(ward).append(", ");
        if (province != null) address.append(province);

        return address.toString();
    }
}