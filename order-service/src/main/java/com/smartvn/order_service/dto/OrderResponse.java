package com.smartvn.order_service.dto;


import com.smartvn.order_service.dto.order.OrderItemDTO;
import com.webanhang.team_project.dto.address.AddressDTO;
import com.webanhang.team_project.dto.payment.PaymentDetailDTO;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String status;
    private int totalAmount;
    private int totalDiscountedPrice;
    private int totalItems;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private AddressDTO shippingAddress;
    private List<OrderItemDTO> orderItems;
    private List<PaymentDetailDTO> paymentDetails;
} 