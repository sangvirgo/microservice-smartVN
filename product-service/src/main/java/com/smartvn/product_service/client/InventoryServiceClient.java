package com.smartvn.product_service.client;

import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// ✅ Thêm fallback
@FeignClient(name = "inventory-service", fallback = InventoryServiceFallback.class)
public interface InventoryServiceClient {

    @GetMapping("/api/v1/internal/inventories/product/{productId}")
    List<Product.InventoryInfo> getAllInventoriesByProduct(@PathVariable("productId") Long productId);

    @GetMapping("/api/v1/inventories/product/{productId}/variants")
    List<ProductDetailDTO.PriceVariantDTO> getProductPriceVariants(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon
    );
}