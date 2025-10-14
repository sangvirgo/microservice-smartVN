package com.smartvn.order_service.dto;

import com.webanhang.team_project.dto.user.UserDTO;
import com.webanhang.team_project.model.Order;
import lombok.Data;

@Data
public class OrderDetailDTO extends OrderDTO {
    private UserDTO user;

    public OrderDetailDTO(Order order) {
        super(order); // Gọi constructor của lớp cha

        // Bổ sung thông tin user
        if (order.getUser() != null) {
            this.user = new UserDTO(order.getUser());
        }
    }
}
