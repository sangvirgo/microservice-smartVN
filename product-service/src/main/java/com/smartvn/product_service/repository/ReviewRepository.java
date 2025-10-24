package com.smartvn.product_service.repository;

import com.smartvn.product_service.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    /**
     * Lấy tất cả các đánh giá của một sản phẩm (phân trang).
     *
     * @param productId ID của sản phẩm.
     * @param pageable  Thông tin phân trang.
     * @return Page chứa các Review.
     */
    Page<Review> findByProductId(Long productId, Pageable pageable);

    /**
     * Lấy tất cả các đánh giá của một sản phẩm (không phân trang).
     * Dùng cho các tác vụ quản trị.
     *
     * @param productId ID của sản phẩm.
     * @return List các Review.
     */
    List<Review> findAllByProductId(Long productId);

    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);
}