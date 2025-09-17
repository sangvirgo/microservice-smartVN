package com.webanhang.team_project.dto.order;

import com.webanhang.team_project.enums.PaymentMethod;
import com.webanhang.team_project.enums.PaymentStatus;
import com.webanhang.team_project.dto.address.AddressDTO;
import com.webanhang.team_project.enums.OrderStatus;
import com.webanhang.team_project.model.Order;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private OrderStatus orderStatus; // Thêm orderStatus
    private Integer totalDiscountedPrice; // Sửa thành Integer để đồng bộ với Order
    private int discount; // Thêm discount
    private int totalItems;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private Integer originalPrice;
    private AddressDTO shippingAddress;
    private PaymentStatus paymentStatus;
    private List<OrderItemDTO> orderItems;
    private PaymentMethod paymentMethod; // Thêm paymentMethod

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.originalPrice = order.getOriginalPrice();
        this.orderStatus = order.getOrderStatus();
        this.totalDiscountedPrice = order.getTotalDiscountedPrice() != null ? order.getTotalDiscountedPrice() : 0;
        this.discount = order.getDiscount();
        this.totalItems = order.getTotalItems();
        this.orderDate = order.getOrderDate();
        this.deliveryDate = order.getDeliveryDate();
        // Kiểm tra null trước khi tạo đối tượng AddressDTO
        this.shippingAddress = order.getShippingAddress() != null ? new AddressDTO(order.getShippingAddress()) : null;
        this.paymentStatus = order.getPaymentStatus();
        this.orderItems = new ArrayList<>();
        this.paymentMethod = order.getPaymentMethod(); // Thêm paymentMethod
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item != null) {
                    this.orderItems.add(new OrderItemDTO(item));
                }
            });
        }
    }
}