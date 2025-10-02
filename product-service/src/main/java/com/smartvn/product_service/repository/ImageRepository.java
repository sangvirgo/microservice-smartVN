package com.smartvn.product_service.repository;

import com.smartvn.product_service.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByProductId(Long productId);
    void deleteByProductId(Long productId);
}
