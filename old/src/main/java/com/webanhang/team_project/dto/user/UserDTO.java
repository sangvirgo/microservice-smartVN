package com.webanhang.team_project.dto.user;

import com.webanhang.team_project.model.Address;
import com.webanhang.team_project.model.User;
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
    private boolean banned;
    private List<Address> addresses;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String oauthProvider;
    private long orderCount;
    private BigDecimal totalSpent;

    // constructor for orderdetailDTO
    public UserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.mobile = user.getPhone();
    }
}

