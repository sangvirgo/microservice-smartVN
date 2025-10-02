package com.smartvn.product_service.controller;


import com.cloudinary.Api;
import com.smartvn.product_service.dto.category.CategoryDTO;
import com.smartvn.product_service.model.Category;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.service.category.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/categories")
public class CategoryController {
    private final ICategoryService categoryService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse> getAllByParentAndSub() {
        List<Category> categories = categoryService.getAllParentCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(categoryDTOs, "Get all categories success"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok().body(ApiResponse.success(categories,"Get success"));
    }

    @GetMapping("/parent")
    public ResponseEntity<ApiResponse> getAllParentCategories() {
        List<Category> parentCategories = categoryService.getAllParentCategories();
        return ResponseEntity.ok().body(ApiResponse.success(parentCategories,"Get parent categories success"));
    }

    @GetMapping("/{topCategory}")
    public ResponseEntity<ApiResponse> getChildTopCategories(@PathVariable("topCategory") String topCategory) {
        List<Category> childCategories = categoryService.getChildTopCategories(topCategory);
        return ResponseEntity.ok().body(ApiResponse.success(childCategories,"Get child categories success"));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse> getCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(categoryDTOs, "Get categories successfully"));
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO(category);
        return dto;
    }

}
