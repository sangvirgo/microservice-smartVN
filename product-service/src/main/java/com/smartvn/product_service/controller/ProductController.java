package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.BulkProductRequest;
import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.ProductListingDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products") // Cập nhật đường dẫn cho phù hợp với Gateway
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * API để lấy danh sách sản phẩm (phân trang) và hỗ trợ tìm kiếm, lọc.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductListingDTO>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {

        Page<ProductListingDTO> productPage = productService.searchProducts(
                keyword, categoryId, minPrice, maxPrice, pageable
        );

        ApiResponse<Page<ProductListingDTO>> response = ApiResponse.<Page<ProductListingDTO>>builder()
                .message("Products fetched successfully.")
                .data(productPage)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * API để lấy thông tin chi tiết của một sản phẩm.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductDetail(@PathVariable Long id) {
        ProductDetailDTO productDetail = productService.getProductDetail(id);

        ApiResponse<ProductDetailDTO> response = ApiResponse.<ProductDetailDTO>builder()
                .message("Product detail fetched successfully.")
                .data(productDetail)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * API để nhập hàng loạt sản phẩm
     * TODO: Thêm @PreAuthorize("hasRole('ADMIN')") khi deploy production
     */
    @PostMapping("/create-multiple")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createMultipleProducts(
            @RequestBody BulkProductRequest request) {

        int totalRequested = request.getProducts() != null ? request.getProducts().size() : 0;
        log.info("📦 Received bulk product creation request with {} items", totalRequested);

        List<Product> createdProducts = productService.createBulkProducts(request.getProducts());

        Map<String, Object> result = new HashMap<>();
        result.put("totalRequested", totalRequested);
        result.put("totalCreated", createdProducts.size());
        result.put("totalFailed", totalRequested - createdProducts.size());
        result.put("products", createdProducts);

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .message(String.format("Bulk import completed: %d/%d products created successfully",
                        createdProducts.size(), totalRequested))
                .data(result)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}