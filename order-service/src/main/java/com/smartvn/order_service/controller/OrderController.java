package com.smartvn.order_service.controller;

import com.smartvn.order_service.dto.order.OrderDTO;
import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.model.Cart;
import com.smartvn.order_service.model.Order;
import com.smartvn.order_service.dto.response.ApiResponse;
import com.smartvn.order_service.model.User;
import com.smartvn.order_service.repository.CartRepository;
import com.smartvn.order_service.security.otp.OtpService;
import com.smartvn.order_service.service.order.IOrderService;
import com.smartvn.order_service.service.user.UserService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;
    private final UserService userService;
    private final OtpService otpService;
    private final CartRepository cartRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @GetMapping("/user")
    private ResponseEntity<ApiResponse> getUserOrders(@RequestHeader("Authorization") String jwt){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ( authentication == null ){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) { // Kiểm tra user từ JWT
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid user session."));
        }

        List<Order> orders = orderService.userOrderHistory(user.getId(), null);
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO(order);
            orderDTOs.add(orderDTO);
        }
        return ResponseEntity.ok(ApiResponse.success(orderDTOs, "Lấy lịch sử đơn hàng thành công!"));
    }

    @PostMapping("/create/{addressId}")
    public ResponseEntity<?> createOrder(@RequestHeader("Authorization") String jwt,
                                         @PathVariable("addressId") Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ( authentication == null ){
            log.warn("Unauthorized attempt to create order: No authentication found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Vui lòng đăng nhập để đặt hàng.", "code", "UNAUTHORIZED"));
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            log.warn("Unauthorized attempt to create order: User not found for JWT.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.", "code", "INVALID_SESSION"));
        }

        try {
            boolean addressExists = user.getAddress().stream()
                    .anyMatch(address -> address.getId().equals(addressId));

            if (!addressExists) {
                log.warn("Address ID {} not found for user {}", addressId, user.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Địa chỉ giao hàng không tìm thấy hoặc không thuộc về bạn.", "code", "ADDRESS_NOT_FOUND"));
            }

            List<Order> orders = orderService.placeOrder(addressId, user);

            if (orders == null || orders.isEmpty()) {
                log.warn("Order creation resulted in no orders for user {}, addressId {}. This might be due to all cart items having null products or other issues.", user.getEmail(), addressId);
                // Kiểm tra lại giỏ hàng để đưa ra thông báo chính xác hơn
                Cart cart = cartRepository.findByUserId(user.getId());
                if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Giỏ hàng của bạn đang trống. Không thể tạo đơn hàng.", "code", "EMPTY_CART_ON_CREATE"));
                }
                // Nếu giỏ hàng không rỗng nhưng không tạo được đơn, có thể do tất cả product trong cart item đều null hoặc không xử lý được
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Không có sản phẩm hợp lệ nào trong giỏ hàng của bạn để đặt hàng. Vui lòng kiểm tra lại.", "code", "NO_VALID_ITEMS_FOR_ORDER"));
            }

            List<OrderDTO> orderDTOs = orders.stream()
                    .map(OrderDTO::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderDTOs);
            response.put("totalOrdersCreated", orders.size());
            response.put("totalAmountForAllOrders", orders.stream()
                    .mapToInt(order -> order.getTotalDiscountedPrice() != null ? order.getTotalDiscountedPrice() : 0)
                    .sum());
            response.put("message", "Đã tạo thành công " + orders.size() + " đơn hàng.");
            if (orders.stream().anyMatch(order -> order.getSellerId() == null)) {
                response.put("message", response.get("message") + " Một số sản phẩm không có thông tin người bán cụ thể đã được gom vào một đơn hàng chung.");
            }


            log.info("Successfully created {} order(s) for user {}", orders.size(), user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Error creating order for user {}: {}", (user != null ? user.getEmail() : "unknown_user_due_to_null"), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage(), "code", "INVALID_ARGUMENT"));
        } catch (RuntimeException e) {
            log.error("Runtime error creating order for user {}: {}", (user != null ? user.getEmail() : "unknown_user_due_to_null"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage(), "code", "ORDER_PROCESSING_ERROR"));
        } catch (Exception e) {
            log.error("Unexpected error creating order for user {}: {}", (user != null ? user.getEmail() : "unknown_user_due_to_null"), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Đã xảy ra lỗi không mong muốn trong quá trình xử lý đơn hàng. Vui lòng thử lại sau.", "code", "INTERNAL_SERVER_ERROR"));
        }
    }

    // ... (các phương thức khác giữ nguyên)
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> findOrderById(@PathVariable("id") Long orderId) {
        Order order = orderService.findOrderById(orderId);
        OrderDTO orderDTO = new OrderDTO(order);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse> getPendingOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid user session."));
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.PENDING);
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO(order);
            orderDTOs.add(orderDTO);
        }
        return ResponseEntity.ok(ApiResponse.success(orderDTOs, "Lấy danh sách đơn hàng chờ xử lý thành công!"));
    }

    @GetMapping("/confirmed")
    public ResponseEntity<ApiResponse> getConfirmedOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid user session."));
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.CONFIRMED);
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO(order);
            orderDTOs.add(orderDTO);
        }
        return ResponseEntity.ok(ApiResponse.success(orderDTOs, "Lấy danh sách đơn hàng đã xác nhận thành công!"));
    }

    @GetMapping("/shipped")
    public ResponseEntity<ApiResponse> getShippedOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid user session."));
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.SHIPPED);
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO(order);
            orderDTOs.add(orderDTO);
        }
        return ResponseEntity.ok(ApiResponse.success(orderDTOs, "Lấy danh sách đơn hàng đang giao thành công!"));
    }

    @GetMapping("/delivered")
    public ResponseEntity<ApiResponse> getDeliveredOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid user session."));
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.DELIVERED);
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO(order);
            orderDTOs.add(orderDTO);
        }
        return ResponseEntity.ok(ApiResponse.success(orderDTOs, "Lấy danh sách đơn hàng đã giao thành công!"));
    }

    @GetMapping("/cancelled")
    public ResponseEntity<ApiResponse> getCancelledOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid user session."));
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.CANCELLED);
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO(order);
            orderDTOs.add(orderDTO);
        }
        return ResponseEntity.ok(ApiResponse.success(orderDTOs, "Lấy danh sách đơn hàng đã hủy thành công!"));
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable("id") Long orderId, @RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ."));
        }
        Order order = orderService.findOrderById(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy đơn hàng."));
        }
        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền hủy đơn hàng này."));
        }

        Order cancelledOrder = orderService.cancelOrder(orderId);
        OrderDTO orderDTO = new OrderDTO(cancelledOrder);
        return ResponseEntity.ok(ApiResponse.success(orderDTO, "Hủy đơn hàng thành công."));
    }


    @PostMapping("/send-mail/{orderId}")
    public ResponseEntity<ApiResponse> sendMail(@RequestHeader("Authorization") String jwt,
                                                @PathVariable("orderId") Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not found"));
        }

        try {
            Order order = orderService.findOrderById(orderId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order not found"));
            }

            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You don't have permission to access this order"));
            }

            otpService.sendOrderMail(user.getEmail(), order);
            return ResponseEntity.ok(ApiResponse.success(null, "Email sent successfully"));
        } catch (Exception e) {
            log.error("Error sending email for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send email: " + e.getMessage()));
        }
    }
}