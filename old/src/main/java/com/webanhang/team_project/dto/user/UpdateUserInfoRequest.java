package com.webanhang.team_project.dto.user;

import lombok.Data;

@Data
public class UpdateUserInfoRequest {
    private String firstName;
    private String lastName;
    private String mobile;
}
