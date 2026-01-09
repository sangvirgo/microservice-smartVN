package com.smartvn.product_service.service;

import com.smartvn.product_service.exceptions.AppException;
import com.smartvn.product_service.model.Category;
import com.smartvn.product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Tạo một danh mục mới.
     * Tự động xử lý việc tạo danh mục cha (level 1) và danh mục con (level 2).
     *
     * @param name       Tên của danh mục cần tạo.
     * @param parentName Tên của danh mục cha (null nếu là danh mục cấp 1).
     * @return Entity Category đã được tạo.
     */
    @Transactional
    public Category createCategory(String name, String parentName) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new AppException("Category with name '" + name + "' already exists.", HttpStatus.BAD_REQUEST);
        }

        Category newCategory = new Category();
        newCategory.setName(name);
        newCategory.setIsParent(true); // Mặc định là parent, sẽ thay đổi nếu có parentName

        if (parentName != null && !parentName.isEmpty()) {
            // Đây là danh mục cấp 2
            Category parentCategory = categoryRepository.findByName(parentName)
                    .orElseThrow(() -> new AppException("Parent category '" + parentName + "' not found.", HttpStatus.NOT_FOUND));

            if (parentCategory.getLevel() != 1) {
                throw new AppException("Parent category must be a level 1 category.", HttpStatus.BAD_REQUEST);
            }

            newCategory.setParentCategory(parentCategory);
            newCategory.setLevel(2);
            newCategory.setIsParent(false);
        } else {
            // Đây là danh mục cấp 1
            newCategory.setLevel(1);
        }

        log.info("Creating new category: {}", newCategory.getName());
        return categoryRepository.save(newCategory);
    }

    /**
     * Tìm một danh mục theo tên.
     *
     * @param name Tên danh mục.
     * @return Entity Category.
     */
    public Category findCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new AppException("Category not found with name: " + name, HttpStatus.NOT_FOUND));
    }

    /**
     * Tìm một danh mục theo ID.
     *
     * @param id ID danh mục.
     * @return Entity Category.
     */
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Category not found with id: " + id, HttpStatus.NOT_FOUND));
    }

    /**
     * Lấy tất cả các danh mục trong hệ thống.
     *
     * @return List các Category.
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Xóa một danh mục.
     * Sẽ báo lỗi nếu danh mục này đang được sử dụng bởi bất kỳ sản phẩm nào.
     *
     * @param categoryId ID của danh mục cần xóa.
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = findCategoryById(categoryId);

        // Kiểm tra xem danh mục có sản phẩm nào không
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new AppException("Cannot delete category. It is currently in use by " + category.getProducts().size() + " products.", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra xem danh mục cha có danh mục con nào không
        if (category.getIsParent() && category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            throw new AppException("Cannot delete parent category with active sub-categories.", HttpStatus.BAD_REQUEST);
        }

        log.warn("Deleting category: {}", category.getName());
        categoryRepository.delete(category);
    }
}