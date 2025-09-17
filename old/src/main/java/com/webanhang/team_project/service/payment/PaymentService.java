package com.webanhang.team_project.service.payment;

import com.webanhang.team_project.model.PaymentDetail;

import java.util.Map;

public interface PaymentService {
    String createPayment(Long orderId) ;
    PaymentDetail getPaymentById(Long paymentId) ;
    PaymentDetail processPaymentCallback(Map<String, String> vnpParams) ;
} 