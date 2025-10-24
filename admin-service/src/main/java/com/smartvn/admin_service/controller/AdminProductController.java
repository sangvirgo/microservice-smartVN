package com.smartvn.admin_service.controller;

import com.smartvn.admin_service.dto.product.InventoryDTO;
import com.smartvn.admin_service.dto.product.ProductAdminViewDTO;
import com.smartvn.admin_service.dto.product.UpdateInventoryRequest;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final AdminProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive) {

        Page<ProductAdminViewDTO> products =
                productService.getAllProducts(page, size, search, categoryId, isActive);
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved"));
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<?>> toggleActive(@PathVariable Long id) {
        productService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted"));
    }

    @PostMapping("/{productId}/inventory")
    public ResponseEntity<ApiResponse<?>> addInventory(
            @PathVariable Long productId,
            @RequestBody UpdateInventoryRequest request) {

        InventoryDTO result = productService.addInventory(productId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Inventory added"));
    }

    @PutMapping("/{productId}/inventory/{inventoryId}")
    public ResponseEntity<ApiResponse<?>> updateInventory(
            @PathVariable Long productId,
            @PathVariable Long inventoryId,
            @RequestBody UpdateInventoryRequest request) {

        InventoryDTO result = productService.updateInventory(productId, inventoryId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Inventory updated"));
    }
}