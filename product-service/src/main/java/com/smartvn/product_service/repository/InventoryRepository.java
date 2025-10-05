package com.smartvn.product_service.repository;

import com.smartvn.product_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    List<Inventory> findAllByProductId(@Param("productId") Long productId);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.store.id = :storeId")
    List<Inventory> findByProductIdAndStoreId(@Param("productId") Long productId,
                                              @Param("storeId") Long storeId);

    @Query("SELECT i FROM Inventory i WHERE i.store.id = :storeId")
    List<Inventory> findByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.store.id = :storeId AND i.size = :size")
    List<Inventory> findByProductIdAndStoreIdAndSize(@Param("productId") Long productId,
                                                     @Param("storeId") Long storeId,
                                                     @Param("size") String size);
}