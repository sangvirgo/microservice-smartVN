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

    // =================================================================
    // 1. CÁC METHOD CỐT LÕI (CORE METHODS)
    // =================================================================

    /**
     * Tìm sản phẩm theo ID và phải đang active.
     * Dùng cho trang chi tiết sản phẩm của khách hàng.
     */
    Optional<Product> findByIdAndIsActiveTrue(Long id);

    /**
     * Lấy danh sách tất cả sản phẩm đang active cho trang listing (phân trang).
     */
    Page<Product> findAllByIsActiveTrue(Pageable pageable);


    // =================================================================
    // 2. CÁC METHOD TÌM KIẾM & LỌC NÂNG CAO (CHO CUSTOMER)
    // =================================================================

    /**
     * Query chính để tìm kiếm và lọc sản phẩm cho trang danh sách.
     * Hoạt động hiệu quả trên các trường đã được cache (giá, tồn kho).
     *
     * @param keyword    Từ khóa tìm kiếm trong tiêu đề sản phẩm (có thể null).
     * @param categoryId ID của danh mục cần lọc (có thể null).
     * @param minPrice   Giá tối thiểu để lọc (dựa trên min_discounted_price, có thể null).
     * @param maxPrice   Giá tối đa để lọc (dựa trên max_discounted_price, có thể null).
     * @param pageable   Thông tin phân trang.
     * @return Page chứa các sản phẩm đã được lọc.
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true AND " +
            "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.minDiscountedPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.maxDiscountedPrice <= :maxPrice)")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Tìm sản phẩm theo một danh sách các ID danh mục.
     * Hữu ích khi muốn hiển thị sản phẩm của cả danh mục cha và các danh mục con.
     */
    List<Product> findByCategoryIdInAndIsActiveTrue(List<Long> categoryIds);

    /**
     * Tìm các sản phẩm bán chạy nhất dựa trên trường quantitySold.
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.quantitySold DESC")
    List<Product> findTopSellingProducts(Pageable pageable);


    // =================================================================
    // 3. CÁC METHOD LẤY THÔNG TIN ĐỂ FILTER (CHO BỘ LỌC UI)
    // =================================================================

    /**
     * Lấy danh sách các danh mục cấp 1 (top-level) có sản phẩm.
     */
    @Query("SELECT DISTINCT c.name FROM Product p JOIN p.category c WHERE p.isActive = true AND c.level = 1 ORDER BY c.name")
    List<String> findDistinctTopLevelCategories();

    /**
     * Lấy danh sách các danh mục cấp 2 theo danh mục cha.
     */
    @Query("SELECT DISTINCT c.name FROM Product p JOIN p.category c " +
            "WHERE p.isActive = true AND c.level = 2 AND c.parentCategory.name = :parentCategoryName ORDER BY c.name")
    List<String> findDistinctSecondLevelCategories(@Param("parentCategoryName") String parentCategoryName);

    /**
     * Lấy danh sách các thương hiệu (brand) có trong hệ thống.
     */
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.isActive = true AND p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();
}