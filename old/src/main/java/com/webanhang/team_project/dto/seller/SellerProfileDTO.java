package com.webanhang.team_project.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private ShopInfoDTO shopInfo;
    private boolean verified;
    private String createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopInfoDTO {
        private String shopName;
        private String logo;
        private String description;
        private String website;
        private String address;
        private String city;
        private String state;
        private String zipCode;
        private String phoneNumber;
        private String businessType;
    }
}
