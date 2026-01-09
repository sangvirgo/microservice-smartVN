package com.smartvn.product_service.repository;

import com.smartvn.product_service.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    Page<Product> findAllByIsActiveTrue(Pageable pageable);

    /**
     * Tìm sản phẩm theo danh sách category IDs (dùng cho các API khác nếu cần)
     */
    List<Product> findByCategoryIdInAndIsActiveTrue(List<Long> categoryIds);

    /**
     * Lấy top sản phẩm bán chạy
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.quantitySold DESC")
    List<Product> findTopSellingProducts(Pageable pageable);

    /**
     * Kiểm tra product có tồn tại không
     */
    boolean existsById(Long id);

    boolean existsByTitleAndBrand(String title, String brand);
    long countByIsActive(boolean isActive);
}