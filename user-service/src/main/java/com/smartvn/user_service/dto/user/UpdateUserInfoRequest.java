package com.smartvn.user_service.dto.user;

import lombok.Data;

@Data
public class UpdateUserInfoRequest {
    private String firstName;
    private String lastName;
    private String mobile;
}
