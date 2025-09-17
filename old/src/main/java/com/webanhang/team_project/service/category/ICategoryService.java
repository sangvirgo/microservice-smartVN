package com.webanhang.team_project.service.category;


import com.webanhang.team_project.model.Category;

import java.util.List;

public interface ICategoryService {
    Category addCategory(Category category);
    Category updateCategory(Category category);


    List<Category> getAllCategories();
    Category findCategoryByName(String name);
    Category findCategoryById(Long categoryId);

    List<Category> getAllParentCategories();

    List<Category> getChildTopCategories(String topCategory);

//    List<Category> getAllCategories();
}
