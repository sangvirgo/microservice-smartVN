package com.smartvn.product_service.repository;

import com.smartvn.product_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findAllByProductId(Long productId);
    List<Inventory> findByProductIdAndStoreId(Long productId, Long storeId);
    List<Inventory> findByStoreId(Long storeId);
    List<Inventory> existsByProductIdAndStoreIdAndSize(Long productId, Long storeId, String size);
}
