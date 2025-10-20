package com.smartvn.order_service.repository;

import com.smartvn.order_service.enums.PaymentMethod;
import com.smartvn.order_service.enums.PaymentStatus;
import com.smartvn.order_service.model.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho PaymentDetail entity
 * Quản lý thông tin thanh toán
 */
@Repository
public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {

    /**
     * Tìm payment detail theo orderId (One-to-One)
     */
    Optional<PaymentDetail> findByOrderId(Long orderId);

    /**
     * Tìm payment details theo payment method
     */
    // ✅ SỬA: Thay String bằng PaymentMethod
    List<PaymentDetail> findByPaymentMethod(PaymentMethod paymentMethod);

    /**
     * Tìm payment details theo payment status
     */
    // ✅ SỬA: Thay String bằng PaymentStatus
    List<PaymentDetail> findByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Tìm payment details trong khoảng thời gian
     */
    @Query("SELECT pd FROM PaymentDetail pd WHERE pd.paymentDate BETWEEN :startDate AND :endDate ORDER BY pd.paymentDate DESC")
    List<PaymentDetail> findByPaymentDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Tìm payment details theo userId (thông qua Order)
     */
    @Query("SELECT pd FROM PaymentDetail pd WHERE pd.order.userId = :userId ORDER BY pd.createdAt DESC")
    List<PaymentDetail> findByUserId(@Param("userId") Long userId);

    /**
     * Kiểm tra order đã có payment detail chưa
     */
    boolean existsByOrderId(Long orderId);

    /**
     * Đếm số lượng payment theo method
     */
    // ✅ SỬA: Thay String bằng PaymentMethod
    @Query("SELECT COUNT(pd) FROM PaymentDetail pd WHERE pd.paymentMethod = :method")
    Long countByPaymentMethod(@Param("method") PaymentMethod method);

    /**
     * Tính tổng tiền theo payment method và status
     */
    // ✅ SỬA: Thay String bằng PaymentMethod và PaymentStatus
    @Query("SELECT COALESCE(SUM(pd.totalAmount), 0) FROM PaymentDetail pd " +
            "WHERE pd.paymentMethod = :method AND pd.paymentStatus = :status")
    Double sumTotalAmountByMethodAndStatus(
            @Param("method") PaymentMethod method,
            @Param("status") PaymentStatus status
    );

    Optional<PaymentDetail> findByTransactionId(String vnp_TxnRef);
}