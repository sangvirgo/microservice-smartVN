package com.smartvn.order_service.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteractionExportDTO {
    private String user_id;
    private String product_id;
    private Float weight;
    private String type;
}