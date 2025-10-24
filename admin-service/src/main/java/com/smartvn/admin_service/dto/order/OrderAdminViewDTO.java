package com.smartvn.admin_service.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderAdminViewDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;

    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;

    private BigDecimal totalPrice;
    private Integer totalItems;

    private Long shippingAddressId;
    private String shippingAddressDetails; // Địa chỉ đầy đủ

    private LocalDateTime createdAt;
    private LocalDateTime deliveryDate;

    private List<OrderItemAdminDTO> orderItems;
}
