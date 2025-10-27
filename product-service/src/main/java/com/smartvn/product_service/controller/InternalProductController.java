package com.smartvn.product_service.controller;


import com.smartvn.product_service.dto.InventoryCheckRequest;
import com.smartvn.product_service.dto.InventoryDTO;
import com.smartvn.product_service.dto.ProductDTO;
import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.admin.ProductStatsDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Inventory;
import com.smartvn.product_service.repository.ProductRepository;
import com.smartvn.product_service.service.InventoryService;
import com.smartvn.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${api.prefix}/internal")
public class InternalProductController {
    private final InventoryService  inventoryService;
    private final ProductService productService;
    private final ProductRepository  productRepository;

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        ProductDetailDTO dto = productService.getProductDetail(productId);
        return ResponseEntity.ok(dto.toSimpleDTO());
    }

    @GetMapping("/products/{productId}/inventory")
    public ResponseEntity<List<InventoryDTO>> getInventory(@PathVariable Long productId) {
        List<Inventory> invs = inventoryService.getInventoriesByProduct(productId);
        List<InventoryDTO> dtos = invs.stream()
                .map(InventoryDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/inventory/check")
    public ResponseEntity<Boolean> checkInventoryAvailability(
            @RequestBody InventoryCheckRequest request) {
        boolean isValid = inventoryService.checkInventoryAvailability(request);

        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/inventory/reduce")
    public ResponseEntity<Void> reduceInventory(
            @RequestBody InventoryCheckRequest request) {

        inventoryService.batchReduceOneInventory(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inventory/restore")
    public ResponseEntity<Void> restoreInventory(
            @RequestBody InventoryCheckRequest request) {

        inventoryService.batchRetoreOneInventory(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inventory/batch-check")
    public ResponseEntity<Map<String, Boolean>> batchCheckInventory(
            @RequestBody List<InventoryCheckRequest> requests) {

        Map<String, Boolean> results = new HashMap<>();

        for (InventoryCheckRequest req : requests) {
            String key = req.getProductId() + "-" + req.getSize();
            boolean hasStock = inventoryService.checkInventoryAvailability(req);
            results.put(key, hasStock);
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/inventory/batch-reduce")
    public ResponseEntity<Void> batchReduceInventory(
            @RequestBody List<InventoryCheckRequest> requests) {

        inventoryService.batchReduceInventory(requests);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/products/{productId}/increase-sold")
    public ResponseEntity<Void> increaseQuantitySold(@RequestBody InventoryCheckRequest request) {

        productService.increaseQuantitySold(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products/stats") // ✅ FIX: Sửa path cho đúng
    public ResponseEntity<ApiResponse<ProductStatsDTO>> getProductStats() {
        ProductStatsDTO stats = new ProductStatsDTO();
        stats.setTotalProducts(productRepository.count());
        stats.setActiveProducts(productRepository.countByIsActive(true));
        return ResponseEntity.ok(ApiResponse.success(stats, "Stats retrieved"));
    }

}
