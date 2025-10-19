package com.smartvn.order_service.repository;

import com.smartvn.order_service.model.Cart;
import com.smartvn.order_service.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho CartItem entity
 * Quản lý các item trong giỏ hàng
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Tìm cart item theo cart và product với size cụ thể
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.size = :size")
    Optional<CartItem> findByCartIdAndProductIdAndSize(
            @Param("cartId") Long cartId,
            @Param("productId") Long productId,
            @Param("size") String size
    );

    /**
     * Kiểm tra cart item đã tồn tại chưa
     */
    boolean existsByCartIdAndProductIdAndSize(Long cartId, Long productId, String size);

    /**
     * Tìm tất cả cart items của một cart
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Tìm tất cả cart items chứa một product
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.productId = :productId")
    List<CartItem> findByProductId(@Param("productId") Long productId);

    /**
     * Xóa tất cả cart items của một cart
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

    /**
     * Xóa cart items theo userId (thông qua cart)
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.userId = :userId")
    void deleteByCartUserId(@Param("userId") Long userId);

    /**
     * Đếm số lượng items trong cart
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long countByCartId(@Param("cartId") Long cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.id IN :itemIds AND ci.cart.id = :cartId")
    List<CartItem> findByIdsAndCartId(
            @Param("itemIds") List<Long> itemIds,
            @Param("cartId") Long cartId
    );
}