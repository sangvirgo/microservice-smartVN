package com.smartvn.admin_service.controller;

import com.smartvn.admin_service.dto.product.*;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    /**
     * ✅ LẤY CHI TIẾT PRODUCT BY ID
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> getProductById(
            @PathVariable Long productId) {

        ProductDetailForEditDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved"));
    }

    /**
     * ✅ TẠO SẢN PHẨM MỚI (SINGLE)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProduct(
            @RequestBody @Valid CreateProductRequest request) {

        ProductAdminViewDTO product = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Product created successfully"));
    }

    /**
     * ✅ TẠO NHIỀU SẢN PHẨM (BULK IMPORT)
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<?>> createBulkProducts(
            @RequestBody @Valid List<CreateProductRequest> requests) {

        Map<String, Object> result = productService.createBulkProducts(requests);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Bulk import completed"));
    }

    /**
     * ✅ CẬP NHẬT PRODUCT INFO
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> updateProduct(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequest request) {

        ProductAdminViewDTO updated = productService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Product updated"));
    }

    /**
     * ✅ UPLOAD ẢNH CHO SẢN PHẨM
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<?>> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = productService.uploadImage(productId, file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        Map.of("imageUrl", imageUrl),
                        "Image uploaded"
                ));
    }

    /**
     * ✅ XÓA ẢNH
     */
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<?>> deleteImage(@PathVariable Long imageId) {
        productService.deleteImage(imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted"));
    }
}