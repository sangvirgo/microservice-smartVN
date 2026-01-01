package com.smartvn.user_service.dto.interaction;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InteractionExportDTO {
    private String user_id;
    private String product_id;
    private Float weight;
    private String type;
}