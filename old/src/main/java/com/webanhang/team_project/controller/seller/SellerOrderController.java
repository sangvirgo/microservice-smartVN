package com.webanhang.team_project.controller.seller;


import com.webanhang.team_project.dto.order.OrderDTO;
import com.webanhang.team_project.dto.order.OrderDetailDTO;
import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.enums.OrderStatus;
import com.webanhang.team_project.model.Order;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.service.seller.ISellerOrderService;
import com.webanhang.team_project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/seller/orders")
public class SellerOrderController {

    private final ISellerOrderService sellerOrderService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse> getSellerOrders(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        System.out.println("=== DEBUG ORDER FILTER ===");
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);
        System.out.println("status: " + status);
        System.out.println("search: " + search);

        User seller = userService.findUserByJwt(jwt);
        Page<Order> orders = sellerOrderService.getSellerOrders(
                seller.getId(), page, size, search, status,
                startDate != null ? LocalDate.parse(startDate) : null,
                endDate != null ? LocalDate.parse(endDate) : null);

        List<OrderDetailDTO> orderDTOs = orders.getContent().stream()
                .map(OrderDetailDTO::new)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderDTOs);
        response.put("pagination", Map.of(
                "currentPage", orders.getNumber(),
                "pageSize", orders.getSize(),
                "totalElements", orders.getTotalElements(),
                "totalPages", orders.getTotalPages(),
                "hasNext", orders.hasNext(),
                "hasPrevious", orders.hasPrevious(),
                "isFirst", orders.isFirst(),
                "isLast", orders.isLast()
        ));

        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách đơn hàng thành công"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderDetails(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId) {

        User seller = userService.findUserByJwt(jwt);
        Order order = sellerOrderService.getOrderDetail(seller.getId(), orderId);
        OrderDetailDTO orderDTO = new OrderDetailDTO(order);

        return ResponseEntity.ok(ApiResponse.success(orderDTO, "Lấy chi tiết đơn hàng thành công"));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        User seller = userService.findUserByJwt(jwt);
        Order updatedOrder = sellerOrderService.updateOrderStatus(seller.getId(), orderId, status);
        OrderDTO orderDTO = new OrderDTO(updatedOrder);

        return ResponseEntity.ok(ApiResponse.success(orderDTO, "Cập nhật trạng thái đơn hàng thành công"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse> getOrderStatistics(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false) String period) {

        User seller = userService.findUserByJwt(jwt);
        Map<String, Object> statistics = sellerOrderService.getOrderStatistics(seller.getId(), period);

        return ResponseEntity.ok(ApiResponse.success(statistics, "Lấy thống kê đơn hàng thành công"));
    }
}
