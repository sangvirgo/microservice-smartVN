package com.smartvn.product_service.repository;

import com.smartvn.product_service.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    Page<Product> findAllByIsActiveTrue(Pageable pageable);

    /**
     * Query chính để tìm kiếm và lọc sản phẩm.
     * Query này JOIN với Inventory để lọc theo giá real-time.
     */
    @Query(value = "SELECT DISTINCT p.* FROM products p " +
            "WHERE p.is_active = true " +
            "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
            "AND (" +
            "  :minPrice IS NULL OR EXISTS (" +
            "    SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price >= :minPrice" +
            "  )" +
            ") " +
            "AND (" +
            "  :maxPrice IS NULL OR EXISTS (" +
            "    SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price <= :maxPrice" +
            "  )" +
            ")",
            countQuery = "SELECT count(DISTINCT p.id) FROM products p " +
                    "WHERE p.is_active = true " +
                    "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
                    "AND (" +
                    "  :minPrice IS NULL OR EXISTS (" +
                    "    SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price >= :minPrice" +
                    "  )" +
                    ") " +
                    "AND (" +
                    "  :maxPrice IS NULL OR EXISTS (" +
                    "    SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price <= :maxPrice" +
                    "  )" +
                    ")",
            nativeQuery = true)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    List<Product> findByCategoryIdInAndIsActiveTrue(List<Long> categoryIds);

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.quantitySold DESC")
    List<Product> findTopSellingProducts(Pageable pageable);

    // Các query khác có thể giữ nguyên hoặc cập nhật nếu cần
}