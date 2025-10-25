package com.smartvn.product_service.repository;

import com.smartvn.product_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    List<Inventory> findAllByProductId(@Param("productId") Long productId);

    List<Inventory> findByProductId(Long productId);

    Optional<Inventory> findByProductIdAndSize(Long productId, String size);

    /**
     * Kiểm tra sản phẩm có còn hàng không (bất kỳ size nào)
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
            "FROM Inventory i WHERE i.product.id = :productId AND i.quantity > 0")
    boolean hasStock(@Param("productId") Long productId);

    long countByProductId(Long productId);
}