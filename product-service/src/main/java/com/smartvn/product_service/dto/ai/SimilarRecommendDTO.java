package com.smartvn.product_service.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarRecommendDTO {
    private List<String> product_ids;  // List product IDs tương tự
    private String strategy;           // "content-based" hoặc "hybrid"
    private Integer count;
}