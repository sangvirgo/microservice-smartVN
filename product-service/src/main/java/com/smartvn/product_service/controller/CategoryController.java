package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.CategoryDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Category;
import com.smartvn.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * API để lấy tất cả danh mục.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        // ✅ CHỈ LẤY CATEGORIES LEVEL 1 (có nested subCategories)
        List<Category> topLevelCategories = categoryService.getAllCategories()
                .stream()
                .filter(cat -> cat.getLevel() == 1) // Chỉ lấy level 1
                .collect(Collectors.toList());

        List<CategoryDTO> categoryDTOs = topLevelCategories.stream()
                .map(CategoryDTO::new) // CategoryDTO tự động map subCategories
                .toList();

        ApiResponse<List<CategoryDTO>> response = ApiResponse.<List<CategoryDTO>>builder()
                .message("Categories fetched successfully.")
                .data(categoryDTOs)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * API để lấy thông tin một danh mục theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.findCategoryById(id);

        ApiResponse<Category> response = ApiResponse.<Category>builder()
                .message("Category fetched successfully.")
                .data(category)
                .build();
        return ResponseEntity.ok(response);
    }

    // --- Các endpoint dưới đây nên được bảo vệ, chỉ dành cho ADMIN ---

    /**
     * API để tạo danh mục mới.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody CreateCategoryRequest request) {
        Category newCategory = categoryService.createCategory(request.getName(), request.getParentName());

        ApiResponse<Category> response = ApiResponse.<Category>builder()
                .message("Category created successfully.")
                .data(newCategory)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * API để xóa một danh mục.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Category with id " + id + " deleted successfully.")
                .build();
        return ResponseEntity.ok(response);
    }

    // DTO nội bộ cho request tạo category
    static class CreateCategoryRequest {
        private String name;
        private String parentName;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getParentName() { return parentName; }
        public void setParentName(String parentName) { this.parentName = parentName; }
    }
}