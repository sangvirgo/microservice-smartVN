package com.smartvn.order_service.controller;

import com.smartvn.order_service.model.Order;
import com.smartvn.order_service.model.PaymentDetail;
import com.smartvn.order_service.service.OrderService;
import com.smartvn.order_service.service.PaymentService;
import com.smartvn.order_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${api.prefix}/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final UserService userService;

    /**
     * Tạo URL thanh toán VNPay cho đơn hàng
     * @param jwt JWT token cho xác thực
     * @param orderId ID của đơn hàng cần thanh toán
     * @return URL thanh toán
     */
    @PostMapping("/create/{orderId}")
    public ResponseEntity<?> createPayment(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId) {
        try {
            // Kiểm tra người dùng và quyền
            Long userId = userService.getUserIdFromJwt(jwt);
            Order order = orderService.findOrderById(orderId);

            if(!order.getUserId().equals(userId)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền truy cập đơn hàng này"));
            }

            String paymentUrl = paymentService.createPayment(orderId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo URL thanh toán thành công",
                    "paymentUrl", paymentUrl
            ));
        } catch (Exception e) {
            log.error("Error creating payment: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xử lý kết quả thanh toán từ VNPay - hỗ trợ cả GET và POST
     * @param params Các tham số nhận được từ VNPay
     * @return Thông tin kết quả thanh toán
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> vnpayCallback(@RequestParam Map<String, String> params) {
        try {
            // ✅ LOG để debug
            log.info("🔔 VNPay callback: txnRef={}, responseCode={}",
                    params.get("vnp_TxnRef"),
                    params.get("vnp_ResponseCode"));

            // Validate required params
            if (params.get("vnp_TxnRef") == null) {
                log.error("Missing vnp_TxnRef. Params: {}", params.keySet());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Thiếu mã giao dịch"));
            }

            // Process callback
            PaymentDetail payment = paymentService.processPaymentCallback(params);
            String responseCode = params.get("vnp_ResponseCode");

            Map<String, Object> response = new HashMap<>();

            if ("00".equals(responseCode)) {
                response.put("success", true);
                response.put("message", "Thanh toán thành công");
                response.put("orderId", payment.getOrder().getId());
                response.put("paymentId", payment.getId());
            } else {
                response.put("success", false);
                response.put("message", "Thanh toán thất bại");
                response.put("responseCode", responseCode);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Payment callback error: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi xử lý thanh toán", "message", e.getMessage()));
        }
    }


    /**
     * Lấy thông tin thanh toán theo ID đơn hàng
     * @param jwt JWT token cho xác thực
     * @param orderId ID của đơn hàng
     * @return Thông tin thanh toán
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentInfo(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId) {
        try {
            Long userId = userService.getUserIdFromJwt(jwt);
            Order order = orderService.findOrderById(orderId);

            // Validate quyền
            if (!order.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền truy cập"));
            }

            // ✅ Sửa tên method
            PaymentDetail payment = order.getPaymentDetail();

            if (payment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Chưa có thông tin thanh toán"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", payment.getId());
            response.put("paymentMethod", payment.getPaymentMethod());
            response.put("paymentStatus", payment.getPaymentStatus());
            response.put("totalAmount", payment.getTotalAmount());
            response.put("transactionId", payment.getTransactionId());
            response.put("paymentDate", payment.getPaymentDate());
            response.put("createdAt", payment.getCreatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting payment info: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi lấy thông tin thanh toán"));
        }
    }
}