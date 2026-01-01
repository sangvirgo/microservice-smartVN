package com.smartvn.admin_service.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để export interaction data cho AI Recommendation Service
 * Format chuẩn để training model collaborative filtering
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteractionExportDTO {

    /**
     * User ID dạng String (có thể là "guest" cho anonymous users)
     */
    private String user_id;

    /**
     * Product ID dạng String
     */
    private String product_id;

    /**
     * Trọng số của interaction (1.0 = VIEW, 3.0 = ADD_TO_CART, 5.0 = PURCHASE)
     */
    private Float weight;

    /**
     * Loại interaction: VIEW, CLICK, ADD_TO_CART, PURCHASE, REVIEW
     */
    private String type;
}
