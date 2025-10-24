package com.smartvn.order_service.client;

import com.smartvn.order_service.dto.product.InventoryCheckRequest;
import com.smartvn.order_service.dto.product.InventoryDTO;
import com.smartvn.order_service.dto.product.InventoryItemDTO;
import com.smartvn.order_service.dto.product.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback khi Product Service không khả dụng
 */
@Component
@Slf4j
public class ProductServiceFallback implements ProductServiceClient {

    @Override
    public ProductDTO getProductById(Long productId) {
        log.error("Product Service unavailable. Returning fallback for productId: {}", productId);

        ProductDTO fallback = new ProductDTO();
        fallback.setId(productId);
        fallback.setTitle("Product Unavailable");
        fallback.setBrand("Unknown");
        fallback.setPrice(BigDecimal.ZERO);
        fallback.setDiscountedPrice(BigDecimal.ZERO);
        fallback.setImages(new ArrayList<>());

        return fallback;
    }

    @Override
    public List<InventoryItemDTO> getProductInventory(Long productId) {
        log.error("Product Service unavailable. Returning empty inventory for productId: {}", productId);
        return new ArrayList<>();
    }

    @Override
    public Boolean checkInventoryAvailability(InventoryCheckRequest request) {
        log.error("Product Service unavailable. Cannot check inventory: {}", request);
        // Trả về false để an toàn - không cho phép đặt hàng khi service down
        return false;
    }

    @Override
    public void reduceInventory(InventoryCheckRequest request) {
        log.error("Product Service unavailable. Cannot reduce inventory: {}", request);
        throw new RuntimeException("Product Service is currently unavailable. Please try again later.");
    }

    @Override
    public void restoreInventory(InventoryCheckRequest request) {
        log.error("Product Service unavailable. Cannot restore inventory: {}", request);
        // Log error nhưng không throw exception vì đây là rollback operation
    }

    // Thiếu method này
    @Override
    public Map<String, Boolean> batchCheckInventory(List<InventoryCheckRequest> requests) {
        log.error("Product Service unavailable. Cannot batch check inventory");
        return new HashMap<>(); // Trả về Map rỗng để an toàn
    }

    // Thiếu method này
    @Override
    public void batchReduceInventory(List<InventoryCheckRequest> requests) {
        log.error("Product Service unavailable. Cannot batch reduce inventory");
        throw new RuntimeException("Product Service is currently unavailable");
    }

    @Override
    public void increaseQuantitySold(InventoryCheckRequest request) {
        log.error("Product Service unavailable. Cannot increase quantity sold");
    }
}