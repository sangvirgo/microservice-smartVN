package com.smartvn.order_service.controller;

import com.smartvn.order_service.client.ProductServiceClient;
import com.smartvn.order_service.client.UserServiceClient;
import com.smartvn.order_service.dto.cart.CreateOrderRequest;
import com.smartvn.order_service.dto.order.OrderDTO;
import com.smartvn.order_service.dto.order.OrderItemDTO;
import com.smartvn.order_service.dto.product.ProductDTO;
import com.smartvn.order_service.dto.user.AddressDTO;
import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.exceptions.AppException;
import com.smartvn.order_service.model.Order;
import com.smartvn.order_service.dto.response.ApiResponse;
import com.smartvn.order_service.service.OrderService;

import com.smartvn.order_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final UserServiceClient  userServiceClient;
    private final ProductServiceClient  productServiceClient;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false) Long orderId,           // ✅ Tìm theo ID cụ thể
            @RequestParam(required = false) OrderStatus status,     // ✅ Lọc theo trạng thái
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,  // ✅ Từ ngày
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate     // ✅ Đến ngày
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        try {
            Long userId = userService.getUserIdFromJwt(jwt);
            userService.validateUser(userId);

            // ✅ GỌI method mới với filters
            List<Order> orders = orderService.searchUserOrders(
                    userId, orderId, status, startDate, endDate
            );

            List<OrderDTO> orderDTOS = orders.stream()
                    .map(OrderDTO::new)
                    .collect(Collectors.toList());

            for (OrderDTO dto : orderDTOS) {
                enrichOrderDTO(dto);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderDTOS);
            response.put("message", "Successfully retrieved order history");
            response.put("total", orderDTOS.size());

            return ResponseEntity.ok(response);

        } catch (AppException e) {
            log.error("Error while getting orders: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("error", e.getMessage(), "code", "ORDER_ERROR"));
        } catch (Exception e) {
            log.error("Unexpected error getting orders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error occurred", "code", "INTERNAL_SERVER_ERROR"));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateOrderRequest request) {
        try {
            Long userId=userService.getUserIdFromJwt(jwt);
            userService.validateUser(userId);

            if(request.getCartItemIds().isEmpty() || request.getCartItemIds()==null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "Vui lòng chọn sản phẩm để đặt hàng",
                                "code", "NO_ITEMS_SELECTED"
                        ));
            }

            if(request.getAddressId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "Vui lòng chọn địa chỉ để đặt hàng",
                                "code", "NO_ADDRESS_SELECTED"
                        ));
            }

            Order order = orderService.placeOrder(
                    userId,
                    request.getAddressId(),
                    request.getCartItemIds()
            );

            if(order==null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Cannot create order. Cart might be empty.",
                                "code", "EMPTY_CART"
                        ));
            }

            OrderDTO orderDTO = new OrderDTO(order);

            enrichOrderDTO(orderDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("order", orderDTO);
            response.put("message", "Đặt hàng thành công!");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AppException e) {
            log.error("Error while creating order.", e.getMessage());
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of(
                            "error", e.getMessage(),
                            "code", "ORDER_ERROR"
                    ));
        } catch (Exception e) {
            log.error("Unexpected error creating order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Unexpected error occurred",
                            "code", "INTERNAL_SERVER_ERROR"
                    ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> findOrderById(@PathVariable("id") Long orderId) {
        Order order = orderService.findOrderById(orderId);
        OrderDTO orderDTO = new OrderDTO(order);
        enrichOrderDTO(orderDTO);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String jwt) {
        try {
            Long userId=userService.getUserIdFromJwt(jwt);
            Order order = orderService.findOrderById(orderId);
            if (!order.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You don't have permission to cancel this order"));
            }
            Order cancelledOrder=orderService.cancelOrder(orderId, userId);
            OrderDTO orderDTO = new OrderDTO(cancelledOrder);

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(orderDTO, "Success", null));
        } catch (AppException e) {
            log.error("Error while cancel order.", e.getMessage());
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of(
                            "error", e.getMessage(), "code", "ORDER_ERROR"
                    ));
        } catch (Exception e) {
            log.error("Unexpected error cancel order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Unexpected error occurred",
                            "code", "INTERNAL_SERVER_ERROR"
                    ));
        }
    }

    private void enrichOrderDTO(OrderDTO orderDTO) {
        // Lấy thông tin address
        try {
            AddressDTO address = userServiceClient.getAddressById(orderDTO.getShippingAddressId());
            orderDTO.setShippingAddress(address);
        } catch (Exception e) {
            log.warn("Failed to fetch address info: {}", e.getMessage());
        }

        // Lấy thông tin product cho từng OrderItem
        for (OrderItemDTO item : orderDTO.getOrderItems()) {
            try {
                ProductDTO product = productServiceClient.getProductById(item.getProductId());
                item.enrichWithProductInfo(product);
            } catch (Exception e) {
                log.warn("Failed to fetch product {} info: {}", item.getProductId(), e.getMessage());
            }
        }
    }
}