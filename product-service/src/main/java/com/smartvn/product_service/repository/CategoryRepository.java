package com.smartvn.product_service.repository;

import com.smartvn.product_service.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Tìm danh mục theo tên.
     *
     * @param name Tên danh mục.
     * @return Optional chứa Category nếu tìm thấy.
     */
    Optional<Category> findByName(String name);

    /**
     * Tìm tất cả các danh mục con của một danh mục cha.
     *
     * @param parentId ID của danh mục cha.
     * @return List các Category con.
     */
    List<Category> findByParentCategoryId(Long parentId);

    /**
     * Tìm tất cả danh mục ở cấp cao nhất (level 1).
     *
     * @return List các Category cấp 1.
     */
    List<Category> findByLevel(Integer level);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    Optional<Category> findByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.level = :level")
    Optional<Category> findByNameAndLevelIgnoreCase(
            @Param("name") String name,
            @Param("level") int level
    );
}