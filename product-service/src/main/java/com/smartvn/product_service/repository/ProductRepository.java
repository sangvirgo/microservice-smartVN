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
    @Query(value = "SELECT DISTINCT p.id FROM products p " +
            "WHERE p.is_active = true " +
            "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.category_id IN (:categoryIds) " +  // ← Bỏ check NULL ở đây
            "AND (" +
            "  :minPrice IS NULL OR EXISTS (" +
            "    SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price >= :minPrice" +
            "  )" +
            ") " +
            "AND (" +
            "  :maxPrice IS NULL OR EXISTS (" +
            "    SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price <= :maxPrice" +
            "  )" +
            ") " +
            "ORDER BY p.quantity_sold DESC",
            nativeQuery = true)
    Page<Product> searchProductsWithCategory(
            @Param("keyword") String keyword,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query(value = "SELECT DISTINCT p.id FROM products p " +
            "WHERE p.is_active = true " +
            "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            // ← Không có filter category
            "AND (" +
            "  :minPrice IS NULL OR EXISTS (" +
            "    SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price >= :minPrice" +
            "  )" +
            ") " +
            "AND (" +
            "  :maxPrice IS NULL OR EXISTS (" +
            "    SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price <= :maxPrice" +
            "  )" +
            ") " +
            "ORDER BY p.quantity_sold DESC",
            nativeQuery = true)
    Page<Product> searchProductsWithoutCategory(
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query(value = "SELECT " +
            "p.id as id, " +
            "p.title as title, " +
            "p.brand as brand, " +
            "p.average_rating as averageRating, " +
            "p.num_ratings as numRatings, " +
            "p.quantity_sold as quantitySold, " +
            "(SELECT i.download_url FROM images i WHERE i.product_id = p.id LIMIT 1) as thumbnailUrl, " +
            "CONCAT(FORMAT(MIN(inv.discounted_price), 0), 'đ - ', FORMAT(MAX(inv.discounted_price), 0), 'đ') as priceRange, " +
            "(SELECT SUM(inv2.quantity) FROM inventory inv2 WHERE inv2.product_id = p.id) > 0 as inStock, " +
            "(SELECT COUNT(*) FROM inventory inv3 WHERE inv3.product_id = p.id) as variantCount, " +
            "(SELECT MAX(inv4.discount_percent) FROM inventory inv4 WHERE inv4.product_id = p.id) > 0 as hasDiscount " +
            "FROM products p " +
            "LEFT JOIN inventory inv ON inv.product_id = p.id " +
            "WHERE p.is_active = true " +
            "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.category_id IN (:categoryIds) " +
            "AND (:minPrice IS NULL OR EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price >= :minPrice)) " +
            "AND (:maxPrice IS NULL OR EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id AND i.discounted_price <= :maxPrice)) " +
            "GROUP BY p.id " +
            "ORDER BY p.quantity_sold DESC",
            nativeQuery = true)
    Page<ProductListingProjection> searchProductsOptimized(
            @Param("keyword") String keyword,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // Tạo interface projection
    interface ProductListingProjection {
        Long getId();
        String getTitle();
        String getBrand();
        Double getAverageRating();
        Integer getNumRatings();
        Long getQuantitySold();
        String getThumbnailUrl();
        String getPriceRange();
        Boolean getInStock();
        Integer getVariantCount();
        Boolean getHasDiscount();
    }

    List<Product> findByCategoryIdInAndIsActiveTrue(List<Long> categoryIds);

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.quantitySold DESC")
    List<Product> findTopSellingProducts(Pageable pageable);

    // Các query khác có thể giữ nguyên hoặc cập nhật nếu cần
}