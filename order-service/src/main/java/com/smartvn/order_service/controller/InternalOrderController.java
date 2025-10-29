package com.smartvn.order_service.controller;


import com.smartvn.order_service.client.ProductServiceClient;
import com.smartvn.order_service.client.UserServiceClient;
import com.smartvn.order_service.dto.admin.OrderAdminViewDTO;
import com.smartvn.order_service.dto.admin.OrderItemAdminDTO;
import com.smartvn.order_service.dto.admin.OrderStatsDTO;
import com.smartvn.order_service.dto.admin.RevenueChartDTO;
import com.smartvn.order_service.dto.product.ProductDTO;
import com.smartvn.order_service.dto.response.ApiResponse;
import com.smartvn.order_service.dto.user.AddressDTO;
import com.smartvn.order_service.dto.user.UserDTO;
import com.smartvn.order_service.enums.OrderStatus;
import com.smartvn.order_service.exceptions.AppException;
import com.smartvn.order_service.model.Order;
import com.smartvn.order_service.model.OrderItem;
import com.smartvn.order_service.repository.OrderRepository;
import com.smartvn.order_service.service.OrderService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/internal/orders")
public class InternalOrderController {
    private final OrderRepository  orderRepository;
    private final OrderService  orderService;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient  productServiceClient;

    @GetMapping("/users/{userId}/products/{productId}/purchased")
    public ResponseEntity<Boolean> hasUserPurchasedProduct(@PathVariable Long userId, @PathVariable Long productId) {
        boolean hasPurchased = orderRepository.existsByUserIdAndProductIdAndDelivered(userId, productId);
        return  ResponseEntity.ok(hasPurchased);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<Page<OrderAdminViewDTO>>> getAllOrdersAdmin(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {

        // ✅ VALIDATE INPUT ở Controller
        if (page < 0 || size < 1 || size > 100) {
            throw new AppException("Invalid pagination params", HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orders = orderService.searchOrdersForAdmin(
                search, status, paymentStatus, startDate, endDate, pageable);

        Page<OrderAdminViewDTO> dtos = orders.map(this::convertToAdminDTO);

        return ResponseEntity.ok(ApiResponse.success(dtos, "Orders retrieved", null));
    }

    private OrderAdminViewDTO convertToAdminDTO(Order order) {
        OrderAdminViewDTO dto = new OrderAdminViewDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setUserEmail(order.getUserEmail());
        dto.setOrderStatus(order.getOrderStatus().name());
        dto.setPaymentStatus(order.getPaymentStatus().name());
        dto.setPaymentMethod(order.getPaymentMethod() != null
                ? order.getPaymentMethod().name()
                : null);
        dto.setTotalPrice(order.getTotalPrice());
        dto.setTotalItems(order.getTotalItems());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setShippingAddressId(order.getShippingAddressId());

        // ✅ FETCH user info (with circuit breaker fallback)
        enrichWithUserInfo(dto, order.getUserId());

        // ✅ FETCH address info (with fallback)
        enrichWithAddressInfo(dto, order.getShippingAddressId());

        // ✅ MAP order items (batch enrich sau)
        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::convertToOrderItemAdminDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private OrderItemAdminDTO convertToOrderItemAdminDTO(OrderItem item) {
        OrderItemAdminDTO dto = new OrderItemAdminDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setSize(item.getSize());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setDiscountedPrice(item.getDiscountedPrice());

        // ✅ Lấy tên product
        try {
            ProductDTO product = productServiceClient.getProductById(item.getProductId());
            dto.setProductTitle(product.getTitle());
        } catch (Exception e) {
            log.warn("Failed to fetch product: {}", e.getMessage());
        }

        return dto;
    }

    /**
     * ✅ CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderAdminViewDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam("status") OrderStatus newStatus) {

        Order updated = orderService.updateOrderStatus(orderId, newStatus);
        OrderAdminViewDTO dto = convertToAdminDTO(updated);

        return ResponseEntity.ok(ApiResponse.success(dto, "Order status updated", null));
    }

    /**
     * ✅ LẤY THỐNG KÊ ĐƠN HÀNG
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<OrderStatsDTO>> getOrderStats(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        OrderStatsDTO stats = orderService.calculateOrderStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats, "Stats retrieved", null));
    }

    @GetMapping("/admin/{orderId}")
    public ResponseEntity<ApiResponse<OrderAdminViewDTO>> getOrderDetailAdmin(
            @PathVariable Long orderId) {

        Order order = orderService.findOrderById(orderId);
        OrderAdminViewDTO dto = convertToAdminDTO(order);

        return ResponseEntity.ok(ApiResponse.success(dto, "Order detail retrieved", null));
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<ApiResponse<RevenueChartDTO>> getRevenueChart(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        RevenueChartDTO chart = orderService.calculateRevenueChart(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(chart, "Revenue chart data", null));
    }


    @CircuitBreaker(name = "userService", fallbackMethod = "userInfoFallback")
    private void enrichWithUserInfo(OrderAdminViewDTO dto, Long userId) {
        try {
            UserDTO user = userServiceClient.getUserById(userId);
            dto.setUserName(user.getFirstName() + " " + user.getLastName());
        } catch (FeignException e) {
            log.warn("Failed to fetch user {}: {}", userId, e.getMessage());
            dto.setUserEmail("Unknown");
            dto.setUserName("Unknown User");
        }
    }

    private void userInfoFallback(OrderAdminViewDTO dto, Long userId, Throwable t) {
        log.error("User service unavailable for userId {}", userId);
        dto.setUserEmail("Service Unavailable");
        dto.setUserName("N/A");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "addressInfoFallback")
    private void enrichWithAddressInfo(OrderAdminViewDTO dto, Long addressId) {
        try {
            AddressDTO address = userServiceClient.getAddressById(addressId);
            dto.setShippingAddressDetails(address.getFullAddress());
        } catch (FeignException e) {
            log.warn("Failed to fetch address {}: {}", addressId, e.getMessage());
            dto.setShippingAddressDetails("Address not available");
        }
    }

    private void addressInfoFallback(OrderAdminViewDTO dto, Long addressId, Throwable t) {
        log.error("User service unavailable for addressId {}", addressId);
        dto.setShippingAddressDetails("N/A");
    }
}
