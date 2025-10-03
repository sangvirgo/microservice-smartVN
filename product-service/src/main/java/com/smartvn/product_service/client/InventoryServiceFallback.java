package com.smartvn.product_service.client;

import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class InventoryServiceFallback implements InventoryServiceClient {

    @Override
    public List<Product.InventoryInfo> getAllInventoriesByProduct(Long productId) {
        log.warn("Fallback: Cannot fetch inventories for product {}. Returning empty list.", productId);
        return Collections.emptyList();
    }

    @Override
    public List<ProductDetailDTO.PriceVariantDTO> getProductPriceVariants(
            Long productId, Double lat, Double lon) {
        log.warn("Fallback: Cannot fetch price variants for product {}. Returning empty list.", productId);
        return Collections.emptyList();
    }
}