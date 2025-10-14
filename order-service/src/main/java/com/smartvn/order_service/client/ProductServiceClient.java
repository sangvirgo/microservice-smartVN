package com.smartvn.order_service.client;

import com.smartvn.order_service.config.FeignClientConfig;
import com.smartvn.order_service.dto.product.InventoryCheckRequest;
import com.smartvn.order_service.dto.product.InventoryDTO;
import com.smartvn.order_service.dto.product.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign Client để giao tiếp với Product Service
 */
@FeignClient(
        name = "product-service",
        fallback = ProductServiceFallback.class,
        configuration = FeignClientConfig.class
)
public interface ProductServiceClient {

    /**
     * Lấy thông tin chi tiết sản phẩm
     */
    @GetMapping("/api/v1/internal/products/{productId}")
    ProductDTO getProductById(@PathVariable("productId") Long productId);

    /**
     * Lấy danh sách inventory của một sản phẩm
     */
    @GetMapping("/api/v1/internal/products/{productId}/inventory")
    List<InventoryDTO> getProductInventory(@PathVariable("productId") Long productId);

    /**
     * Kiểm tra tồn kho có đủ không
     */
    @PostMapping("/api/v1/internal/inventory/check")
    Boolean checkInventoryAvailability(@RequestBody InventoryCheckRequest request);

    /**
     * Giảm số lượng tồn kho khi đặt hàng
     */
    @PostMapping("/api/v1/internal/inventory/reduce")
    void reduceInventory(@RequestBody InventoryCheckRequest request);

    /**
     * Hoàn lại số lượng tồn kho khi hủy đơn
     */
    @PostMapping("/api/v1/internal/inventory/restore")
    void restoreInventory(@RequestBody InventoryCheckRequest request);
}