package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.BulkProductRequest;
import com.smartvn.product_service.dto.InventoryDTO;
import com.smartvn.product_service.dto.admin.ProductAdminViewDTO;
import com.smartvn.product_service.dto.admin.UpdateInventoryRequest;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Inventory;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.service.InventoryService;
import com.smartvn.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/internal/products/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<ProductAdminViewDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive) {

        // Sử dụng lại searchProducts nhưng không filter isActive=true
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.searchProductsForAdmin(search, categoryId, isActive, pageable);
        Page<ProductAdminViewDTO> dtos = products.map(this::convertToAdminDTO);

        return ResponseEntity.ok(ApiResponse.success(dtos, "Products retrieved"));
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id) {
        productService.toggleProductActive(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.softDeleteProduct(id); // Set isActive = false
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted"));
    }

    @PutMapping("/{productId}/inventory/{inventoryId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> updateInventory(
            @PathVariable Long productId,
            @PathVariable Long inventoryId,
            @RequestBody UpdateInventoryRequest request) {

        Inventory updated = inventoryService.updateInventory(inventoryId, request);
        return ResponseEntity.ok(ApiResponse.success(new InventoryDTO(updated), "Inventory updated"));
    }

    @PostMapping("/{productId}/inventory")
    public ResponseEntity<ApiResponse<InventoryDTO>> addInventory(
            @PathVariable Long productId,
            @RequestBody UpdateInventoryRequest request) {

        Inventory inv = inventoryService.addInventory(productId, request);
        return ResponseEntity.ok(ApiResponse.success(new InventoryDTO(inv), "Inventory added"));
    }

    private ProductAdminViewDTO convertToAdminDTO(Product product) {
        ProductAdminViewDTO dto = new ProductAdminViewDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setBrand(product.getBrand());
        dto.setActive(product.getIsActive());
        dto.setWarningCount(product.getWarningCount());
        dto.setQuantitySold(product.getQuantitySold());
        dto.setAverageRating(product.getAverageRating());
        dto.setNumRatings(product.getNumRatings());
        dto.setCreatedAt(product.getCreatedAt());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        // Map inventories
        if (product.getInventories() != null) {
            dto.setInventories(product.getInventories().stream()
                    .map(InventoryDTO::new) // Cần tạo constructor trong InventoryDTO
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}