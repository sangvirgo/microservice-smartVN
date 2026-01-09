package com.smartvn.product_service.dto;

import com.smartvn.product_service.model.Category;
import lombok.Data;
import java.util.List;

@Data
public class CategoryDTO {
    private Long categoryId;
    private String name;
    private int level;

    private List<CategoryDTO> subCategories;

    public CategoryDTO(Category category) {
        this.categoryId = category.getId();
        this.name = category.getName();
        this.level = category.getLevel();
        if (category.getSubCategories() != null) {
            this.subCategories = category.getSubCategories().stream()
                    .map(CategoryDTO::new)
                    .toList();
        }
    }
}