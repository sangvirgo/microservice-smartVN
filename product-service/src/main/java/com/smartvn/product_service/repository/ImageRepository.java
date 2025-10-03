package com.smartvn.product_service.repository;

import com.smartvn.product_service.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Tìm tất cả hình ảnh thuộc về một sản phẩm.
     *
     * @param productId ID của sản phẩm.
     * @return List các Image.
     */
    List<Image> findByProductId(Long productId);
}