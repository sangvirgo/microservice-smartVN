package com.smartvn.order_service.repository;

import com.smartvn.order_service.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho Cart entity
 * Quản lý giỏ hàng của user
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Tìm giỏ hàng theo userId
     */
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    /**
     * Kiểm tra user đã có giỏ hàng chưa
     */
    boolean existsByUserId(Long userId);
}