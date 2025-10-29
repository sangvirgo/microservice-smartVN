package com.smartvn.order_service.repository;

import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.enums.PaymentStatus;
import com.smartvn.order_service.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho Order entity
 * Quản lý đơn hàng
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    // ============================================
    // TÌM KIẾM CƠ BẢN
    // ============================================

    /**
     * Tìm tất cả đơn hàng của user
     */
    List<Order> findByUserId(Long userId);

    /**
     * Tìm đơn hàng theo userId và sắp xếp theo ngày tạo
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Tìm đơn hàng theo userId và status
     */
    List<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);

    /**
     * Tìm đơn hàng theo status
     */
    List<Order> findByOrderStatus(OrderStatus status);

    /**
     * Tìm đơn hàng theo payment status
     */
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);

    // ============================================
    // TÌM KIẾM THEO THỜI GIAN
    // ============================================

    /**
     * Tìm đơn hàng trong khoảng thời gian
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Tìm đơn hàng trong khoảng thời gian với status cụ thể
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.orderStatus = :status ORDER BY o.createdAt DESC")
    List<Order> findByCreatedAtBetweenAndOrderStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") OrderStatus status
    );

    /**
     * Tìm đơn hàng từ một thời điểm trở đi
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.orderStatus = :status")
    List<Order> findByCreatedAtGreaterThanEqualAndOrderStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("status") OrderStatus status
    );

    // ============================================
    // TÌM KIẾM VỚI FILTER & PAGINATION
    // ============================================

    /**
     * Tìm kiếm đơn hàng với nhiều điều kiện (Admin)
     */
    @Query("SELECT o FROM Order o WHERE " +
            "(:search IS NULL OR " +
            "  CAST(o.id AS string) LIKE CONCAT('%', :search, '%') OR " +
            "  LOWER(o.userEmail) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findOrdersWithFilters(
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Tìm kiếm đơn hàng của user với filter
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findUserOrdersWithFilters(
            @Param("userId") Long userId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // ============================================
    // THỐNG KÊ
    // ============================================

    /**
     * Đếm số đơn hàng theo status và khoảng thời gian
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE " +
            "(:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:status IS NULL OR o.orderStatus = :status)")
    Long countOrdersByStatusAndDateRange(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Tính tổng doanh thu từ các đơn hàng đã giao
     */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE " +
            "o.orderStatus = 'DELIVERED' " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate)")
    Double sumRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Đếm số đơn hàng của user
     */
    Long countByUserId(Long userId);

    /**
     * Đếm số đơn hàng của user theo status
     */
    Long countByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);

    // ============================================
    // XÓA
    // ============================================


    @Query("SELECT DATE(o.createdAt) as date, SUM(o.totalPrice) as revenue " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :start AND :end " +
            "AND o.orderStatus = :status " +
            "GROUP BY DATE(o.createdAt) " +
            "ORDER BY DATE(o.createdAt) ASC")
    List<Object[]> findRevenueGroupedByDate(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") OrderStatus status
    );

    /**
     * Kiểm tra user có đơn hàng nào không
     */
    boolean existsByUserId(Long userId);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Order o JOIN o.orderItems oi " +
            "WHERE o.userId = :userId " +
            "AND oi.productId = :productId " +
            "AND o.orderStatus = 'DELIVERED'")
    boolean existsByUserIdAndProductIdAndDelivered(
            @Param("userId") Long userId,
            @Param("productId") Long productId
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE " +
            "(:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate)")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


}