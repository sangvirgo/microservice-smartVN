package com.webanhang.team_project.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerStatusDTO {
    private boolean isActive;
    private String status;
}
