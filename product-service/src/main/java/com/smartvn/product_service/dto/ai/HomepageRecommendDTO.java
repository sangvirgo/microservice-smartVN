package com.smartvn.product_service.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomepageRecommendDTO {
    private List<String> product_ids;  // List product IDs từ AI
    private String strategy;           // "personalized" hoặc "popular"
    private Integer count;
}