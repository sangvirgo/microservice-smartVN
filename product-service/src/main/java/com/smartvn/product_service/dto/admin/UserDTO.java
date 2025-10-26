package com.smartvn.product_service.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String mobile;
    private boolean active;
    private int warningCount;
    private boolean banned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;
    private String oauthProvider;
}
