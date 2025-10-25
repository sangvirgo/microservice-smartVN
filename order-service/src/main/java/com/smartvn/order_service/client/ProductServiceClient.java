package com.smartvn.order_service.client;

import com.smartvn.order_service.config.FeignClientConfig;
import com.smartvn.order_service.dto.product.InventoryCheckRequest;
import com.smartvn.order_service.dto.product.InventoryDTO;
import com.smartvn.order_service.dto.product.InventoryItemDTO;
import com.smartvn.order_service.dto.product.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @GetMapping("${api.prefix}/internal/products/{productId}")
    ProductDTO getProductById(@PathVariable("productId") Long productId);

    /**
     * Lấy danh sách inventory của một sản phẩm
     */
    @GetMapping("${api.prefix}/internal/products/{productId}/inventory")
    List<InventoryItemDTO> getProductInventory(@PathVariable("productId") Long productId);

    /**
     * Kiểm tra tồn kho có đủ không
     */
    @PostMapping("${api.prefix}/internal/inventory/check")
    Boolean checkInventoryAvailability(@RequestBody InventoryCheckRequest request);

    /**
     * Giảm số lượng tồn kho khi đặt hàng
     */
    @PostMapping("${api.prefix}/internal/inventory/reduce")
    void reduceInventory(@RequestBody InventoryCheckRequest request);

    /**
     * Hoàn lại số lượng tồn kho khi hủy đơn
     */
    @PostMapping("${api.prefix}/internal/inventory/restore")
    void restoreInventory(@RequestBody InventoryCheckRequest request);

    @PostMapping("${api.prefix}/internal/inventory/batch-check")
    Map<String, Boolean> batchCheckInventory(@RequestBody List<InventoryCheckRequest> requests);

    @PostMapping("${api.prefix}/internal/inventory/batch-reduce")
    void batchReduceInventory(@RequestBody List<InventoryCheckRequest> requests);

    @PostMapping("${api.prefix}/internal/products/{productId}/increase-sold")
    void increaseQuantitySold(@RequestBody InventoryCheckRequest request);
}