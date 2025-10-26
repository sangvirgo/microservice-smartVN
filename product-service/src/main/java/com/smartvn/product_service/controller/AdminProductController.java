package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.InventoryDTO;
import com.smartvn.product_service.dto.admin.CreateProductRequest;
import com.smartvn.product_service.dto.admin.ProductAdminViewDTO;
import com.smartvn.product_service.dto.admin.UpdateInventoryRequest;
import com.smartvn.product_service.dto.admin.UpdateProductRequest;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Inventory;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.service.InventoryService;
import com.smartvn.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/internal/products/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    /**
     * ✅ TẠO SẢN PHẨM ĐƠN LẺ
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductAdminViewDTO>> createProduct(
            @RequestBody @Valid CreateProductRequest request) {

        log.info("📦 Admin creating single product: {}", request.getTitle());

        Product product = productService.createSingleProduct(request);
        ProductAdminViewDTO dto = convertToAdminDTO(product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Product created successfully"));
    }

    /**
     * ✅ CẬP NHẬT SẢN PHẨM
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductAdminViewDTO>> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProductRequest request) {

        log.info("📝 Admin updating product ID: {}", id);

        Product updated = productService.updateProduct(id, request);
        ProductAdminViewDTO dto = convertToAdminDTO(updated);

        return ResponseEntity.ok(
                ApiResponse.success(dto, "Product updated successfully")
        );
    }

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

        return ResponseEntity.ok(ApiResponse.<Page<ProductAdminViewDTO>>builder()
                .data(dtos)
                .message("Products retrieved")
                .status(HttpStatus.OK.value())
                .build());
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id) {
        productService.toggleProductActive(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .data(null)
                .message("Product status updated")
                .status(HttpStatus.OK.value())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.softDeleteProduct(id); // Set isActive = false
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .data(null)
                .message("Product deleted")
                .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductAdminViewDTO>> getProductDetail(
            @PathVariable Long productId) {
        Product product = productService.findById(productId);
        ProductAdminViewDTO dto = convertToAdminDTO(product);
        return ResponseEntity.ok(ApiResponse.success(dto, "Product retrieved"));
    }

    /**
     * ✅ CẬP NHẬT INVENTORY
     */
    @PutMapping("/{productId}/inventory/{inventoryId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> updateInventory(
            @PathVariable Long productId,
            @PathVariable Long inventoryId,
            @RequestBody @Valid UpdateInventoryRequest request) {

        Inventory updated = inventoryService.updateInventory(inventoryId, request);

        return ResponseEntity.ok(
                ApiResponse.success(new InventoryDTO(updated), "Inventory updated")
        );
    }

    /**
     * ✅ THÊM INVENTORY VARIANT
     */
    @PostMapping("/{productId}/inventory")
    public ResponseEntity<ApiResponse<InventoryDTO>> addInventory(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateInventoryRequest request) {

        Inventory inv = inventoryService.addInventory(productId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(new InventoryDTO(inv), "Inventory added"));
    }

    /**
     * ✅ XÓA INVENTORY VARIANT
     */
    @DeleteMapping("/inventory/{inventoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(
            @PathVariable Long inventoryId) {

        inventoryService.deleteInventory(inventoryId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .data(null)
                .message("Inventory deleted")
                .status(HttpStatus.OK.value())
                .build());
    }



    private ProductAdminViewDTO convertToAdminDTO(Product product) {
        ProductAdminViewDTO dto = new ProductAdminViewDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setBrand(product.getBrand());
        dto.setActive(product.getIsActive());
        dto.setQuantitySold(product.getQuantitySold());
        dto.setAverageRating(product.getAverageRating());
        dto.setNumRatings(product.getNumRatings());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setImageUrl(product.getImages().getFirst().getDownloadUrl());

        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getParentCategory().getName());
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