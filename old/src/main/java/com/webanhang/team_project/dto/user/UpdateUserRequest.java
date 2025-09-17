package com.webanhang.team_project.dto.user;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
