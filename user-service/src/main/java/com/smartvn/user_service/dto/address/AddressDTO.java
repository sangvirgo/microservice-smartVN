package com.smartvn.user_service.dto.address;

import com.smartvn.user_service.model.Address;
import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String province;
    private String ward;
    private String street;
    private String note;

    public AddressDTO(Address address) {
        if (address != null) {
            this.id = address.getId();
            this.fullName = address.getFullName();
            this.phoneNumber = address.getPhoneNumber();
            this.province = address.getProvince();
            this.ward = address.getWard();
            this.street = address.getStreet();
            this.note = address.getNote();
        }
    }
}
