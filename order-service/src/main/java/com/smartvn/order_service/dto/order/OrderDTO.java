package com.smartvn.order_service.dto.order;

import com.smartvn.order_service.dto.user.AddressDTO;
import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.enums.PaymentMethod;
import com.smartvn.order_service.enums.PaymentStatus;
import com.smartvn.order_service.model.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
    private Integer totalDiscountedPrice;
    private Integer discount;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime deliveryDate;
    private Integer originalPrice;
    private Long shippingAddressId;
    private AddressDTO shippingAddress;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private List<OrderItemDTO> orderItems;

    /**
     * Constructor từ Order entity
     */
    public OrderDTO(Order order) {
        this.id = order.getId();
        this.userId = order.getUserId();
        this.shippingAddressId = order.getShippingAddressId();
        this.orderStatus = order.getOrderStatus();
        this.totalPrice = order.getTotalPrice();
        this.paymentStatus = order.getPaymentStatus();
        this.paymentMethod = order.getPaymentMethod();
        this.createdAt = order.getCreatedAt();
        this.deliveryDate = order.getDeliveryDate();

        // Tính toán từ orderItems
        order.calculateTotals();
        this.originalPrice = order.getOriginalPrice();
        this.totalDiscountedPrice = order.getTotalDiscountedPrice();
        this.discount = order.getDiscount();
        this.totalItems = order.getTotalItems();

        // Map order items (chưa có product info)
        if (order.getOrderItems() != null) {
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemDTO::new)
                    .collect(Collectors.toList());
        } else {
            this.orderItems = new ArrayList<>();
        }

        // shippingAddress sẽ được enrich sau bởi service
        this.shippingAddress = null;
    }

    /**
     * Constructor mặc định
     */
    public OrderDTO() {
        this.orderItems = new ArrayList<>();
    }
}