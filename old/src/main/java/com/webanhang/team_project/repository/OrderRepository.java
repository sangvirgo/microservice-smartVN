package com.webanhang.team_project.repository;


import com.webanhang.team_project.enums.OrderStatus;
import com.webanhang.team_project.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByOrderDateBetweenAndOrderStatus(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);
    List<Order> findByOrderDateGreaterThanEqualAndOrderStatus(LocalDateTime startDate, OrderStatus status);
    List<Order> findByOrderStatus(OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    @Modifying
    @Query("DELETE FROM Order o WHERE o.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.shippingAddress")
    List<Order> findAllWithUser();

    List<Order> findOrderByOrderStatus(OrderStatus status);

    List<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.shippingAddress ORDER BY o.orderDate DESC")
    List<Order> findAllWithUserOrderByOrderDateDesc();

    @Query("SELECT SUM(o.totalDiscountedPrice) FROM Order o WHERE o.sellerId = :sellerId AND o.orderStatus = :status")
    Integer sumTotalDiscountedPriceBySellerIdAndOrderStatus(
            @Param("sellerId") Long sellerId, @Param("status") OrderStatus status);

    List<Order> findBySellerId(Long sellerId);
    List<Order> findBySellerIdAndOrderStatus(Long sellerId, OrderStatus status);
    List<Order> findBySellerIdAndOrderDateBetween(Long sellerId, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findBySellerIdAndOrderDateBetweenAndOrderStatus(
            Long sellerId, LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);

    @Query("SELECT o FROM Order o " +
            "JOIN o.orderItems oi " +
            "WHERE oi.product.sellerId = :sellerId " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.orderStatus = :status")
    List<Order> findBySellerIdAndDateRangeAndStatus(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.sellerId = :sellerId " +
            "AND (:search IS NULL OR " +
            "LOWER(CONCAT(o.user.firstName, ' ', o.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(o.id AS string) LIKE CONCAT('%', :search, '%')) " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<Order> findSellerOrdersWithFilters(
            @Param("sellerId") Long sellerId,
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
            "(:search IS NULL OR " +
            "LOWER(CONCAT(o.user.firstName, ' ', o.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(o.id AS string) LIKE CONCAT('%', :search, '%')) " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<Order> findAdminOrdersWithFilters(
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE " +
            "(:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
            "AND (:status IS NULL OR o.orderStatus = :status)")
    Long countOrdersByStatusAndDateRange(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalDiscountedPrice) FROM Order o WHERE " +
            "(:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
            "AND o.orderStatus = 'DELIVERED'")
    Double sumRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

            
}
