package com.smartvn.user_service.dto.auth;

import jakarta.validation.constraints.Email;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;


@Data
public class LoginRequest {

    @NotBlank(message = "Email can't be blank")
    @Email(message = "Email is not valid")
    private String email;
    @NotBlank(message = "Password can't be blank")
    private String password;
}
