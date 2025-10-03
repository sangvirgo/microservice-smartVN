package com.smartvn.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar; // URL avatar nếu có

    // Helper method để lấy tên đầy đủ
    public String getFullName() {
        return firstName + " " + lastName;
    }
}