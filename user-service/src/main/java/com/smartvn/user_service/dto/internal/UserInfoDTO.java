package com.smartvn.user_service.dto.internal;

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
    private String avatar;
    private boolean isActive;
    private boolean isBanned;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}