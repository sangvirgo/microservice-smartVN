package com.smartvn.user_service.dto.address;

import lombok.Data;

@Data
public class AddAddressRequest {
    private String fullName;
    private String phoneNumber;
    private String province;
    private String ward;
    private String street;
    private String note;
}
