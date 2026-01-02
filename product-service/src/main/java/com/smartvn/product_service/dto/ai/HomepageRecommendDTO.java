package com.smartvn.product_service.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomepageRecommendDTO {
    @JsonProperty("product_ids")  // ✅ THÊM annotation này
    private List<String> product_ids;

    private String strategy;           // "personalized" hoặc "popular"
    private Integer count;
}