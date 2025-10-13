package com.smartvn.product_service.service;

import com.smartvn.product_service.model.Inventory;
import com.smartvn.product_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<Inventory> getInventoriesByProduct(Long productId) {
        return inventoryRepository.findAllByProductId(productId);
    }

    public void updateInventoryQuantity(Long inventoryId, Integer quantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() -> new RuntimeException("Inventory not found"));
        inventory.setQuantity(quantity);
        inventoryRepository.save(inventory);
    }

    public void updateInventoryPrice(Long inventoryId, BigDecimal price, Integer discount) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() -> new RuntimeException("Inventory not found"));
        inventory.setPrice(price);
        inventory.setDiscountPercent(discount);
        inventoryRepository.save(inventory);
    }
}