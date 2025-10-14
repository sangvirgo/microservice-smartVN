package com.smartvn.order_service.repository;

import com.webanhang.team_project.model.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentDetail, Long> {
    Optional<PaymentDetail> findByTransactionId(String transactionId);
    Optional<PaymentDetail> findByOrderId(Long orderId);
} 