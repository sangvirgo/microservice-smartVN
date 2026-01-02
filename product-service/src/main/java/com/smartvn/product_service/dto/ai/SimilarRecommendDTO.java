package com.smartvn.product_service.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarRecommendDTO {
    @JsonProperty("product_ids")  // ✅ THÊM annotation này
    private List<String> product_ids;

    private String strategy;           // "content-based" hoặc "hybrid"
    private Integer count;
}