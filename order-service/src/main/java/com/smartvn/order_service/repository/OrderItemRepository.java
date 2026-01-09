package com.smartvn.order_service.repository;

import com.smartvn.order_service.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho OrderItem entity
 * Quản lý các item trong đơn hàng
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Tìm tất cả order items của một order
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Tìm tất cả order items chứa một product
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.productId = :productId")
    List<OrderItem> findByProductId(@Param("productId") Long productId);

    /**
     * Tìm order items theo productId và size
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.productId = :productId AND oi.size = :size")
    List<OrderItem> findByProductIdAndSize(@Param("productId") Long productId, @Param("size") String size);

    /**
     * Xóa order items theo userId (thông qua order)
     */
    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.order.userId = :userId")
    void deleteByOrderUserId(@Param("userId") Long userId);

    /**
     * Đếm số lượng items trong order
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Long countByOrderId(@Param("orderId") Long orderId);

    /**
     * Tính tổng số lượng sản phẩm đã bán (theo productId)
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "WHERE oi.productId = :productId " +
            "AND oi.order.orderStatus = 'DELIVERED'")
    Long sumQuantitySoldByProductId(@Param("productId") Long productId);
}