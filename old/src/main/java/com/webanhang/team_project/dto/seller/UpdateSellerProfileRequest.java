package com.webanhang.team_project.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSellerProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private ShopInfo shopInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopInfo {
        private String shopName;
        private String logo;
        private String description;
        private String website;
        private String address;
        private String city;
        private String state;
        private String zipCode;
        private String phone;
        private String businessType;
    }
}
