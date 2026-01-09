package com.smartvn.user_service.dto.user;

import com.smartvn.user_service.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    
    // Sẽ chứa danh sách Address DTO thay vì Entity
    private List<AddressDTO> addresses;

    // Các trường orderCount và totalSpent sẽ do admin-service tính toán, không thuộc về đây
}