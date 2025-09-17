package com.webanhang.team_project.dto.user;


import com.webanhang.team_project.dto.address.AddressDTO;
import com.webanhang.team_project.dto.cart.CartDTO;
import com.webanhang.team_project.dto.order.OrderDTO;
import com.webanhang.team_project.model.Review;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String mobile;
    private String role;
    private Boolean status;
    private List<AddressDTO> address = new ArrayList<>();
    private List<OrderDTO> orders = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private CartDTO cart;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String oauthProvider;
}

