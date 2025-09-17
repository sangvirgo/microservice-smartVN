package com.webanhang.team_project.dto.address;

import lombok.Data;

@Data
public class AddAddressRequest {
    private String fullName;
    private String phoneNumber;
    private String province;
    private String district;
    private String ward;
    private String street;
    private String note;
}
