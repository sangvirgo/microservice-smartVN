package com.smartvn.product_service.service;

import com.smartvn.product_service.dto.InventoryCheckRequest;
import com.smartvn.product_service.dto.admin.UpdateInventoryRequest;
import com.smartvn.product_service.exceptions.AppException;
import com.smartvn.product_service.model.Inventory;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.repository.InventoryRepository;
import com.smartvn.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

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

    @Transactional
    public void batchReduceInventory(List<InventoryCheckRequest> requests) {
        for(InventoryCheckRequest rq: requests) {
            Inventory inv = inventoryRepository.findByProductIdAndSize(rq.getProductId(), rq.getSize())
                    .orElseThrow(() -> new AppException("Inventory not found", HttpStatus.NOT_FOUND));

            if(inv.getQuantity() <rq.getQuantity()) {
                throw new AppException(
                        String.format("Insufficient stock for product %d size %s",
                                rq.getProductId(), rq.getSize()),
                        HttpStatus.BAD_REQUEST
                );
            }
            inv.setQuantity(inv.getQuantity() - rq.getQuantity());
            inventoryRepository.save(inv);
        }
    }

    public boolean checkInventoryAvailability(InventoryCheckRequest req) {
        Inventory inv = inventoryRepository.findByProductIdAndSize(req.getProductId(), req.getSize())
                .orElse(null);
        return inv != null && inv.getQuantity() >= req.getQuantity();
    }

    @Transactional
    public void batchReduceOneInventory(InventoryCheckRequest rq) {
        Inventory inv = inventoryRepository.findByProductIdAndSize(rq.getProductId(), rq.getSize())
                .orElseThrow(() -> new AppException("Inventory not found", HttpStatus.NOT_FOUND));

        if(inv.getQuantity() <rq.getQuantity()) {
            throw new AppException(
                    String.format("Insufficient stock for product %d size %s",
                            rq.getProductId(), rq.getSize()),
                    HttpStatus.BAD_REQUEST
            );
        }
        inv.setQuantity(inv.getQuantity() - rq.getQuantity());
        inventoryRepository.save(inv);
    }

    @Transactional
    public void batchRetoreOneInventory(InventoryCheckRequest rq) {
        Inventory inv = inventoryRepository.findByProductIdAndSize(rq.getProductId(), rq.getSize())
                .orElseThrow(() -> new AppException("Inventory not found", HttpStatus.NOT_FOUND));

        inv.setQuantity(inv.getQuantity() + rq.getQuantity());
        inventoryRepository.save(inv);
    }

    public Inventory addInventory(Long productId, UpdateInventoryRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));

        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setSize(req.getSize());
        inv.setQuantity(req.getQuantity());
        inv.setPrice(req.getPrice());
        inv.setDiscountPercent(req.getDiscountPercent());

        return inventoryRepository.save(inv);
    }

    public Inventory updateInventory(Long inventoryId, UpdateInventoryRequest req) {
        Inventory inv = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException("Inventory not found", HttpStatus.NOT_FOUND));

        if (req.getSize() != null) inv.setSize(req.getSize());
        if (req.getQuantity() != null) inv.setQuantity(req.getQuantity());
        if (req.getPrice() != null) inv.setPrice(req.getPrice());
        if (req.getDiscountPercent() != null) inv.setDiscountPercent(req.getDiscountPercent());

        return inventoryRepository.save(inv);
    }

    // ✅ THÊM VÀO InventoryService.java

    @Transactional
    public void deleteInventory(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException(
                        "Inventory not found",
                        HttpStatus.NOT_FOUND
                ));

        // Check if this is the last variant
        Long productId = inventory.getProduct().getId();
        long variantCount = inventoryRepository.countByProductId(productId);

        if (variantCount <= 1) {
            throw new AppException(
                    "Cannot delete the last variant. Product must have at least one variant.",
                    HttpStatus.BAD_REQUEST
            );
        }

        inventoryRepository.delete(inventory);
        log.info("✅ Deleted inventory variant: {} - {}",
                productId, inventory.getSize());
    }

}