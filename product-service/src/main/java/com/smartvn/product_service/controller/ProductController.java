package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.ProductListingDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * API để lấy danh sách sản phẩm (phân trang) và hỗ trợ tìm kiếm, lọc.
     * Đây là API chính cho trang danh sách sản phẩm.
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
     * Đây là API cho trang chi tiết sản phẩm.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Double lat, // Vĩ độ của người dùng
            @RequestParam(required = false) Double lon) { // Kinh độ của người dùng

        ProductDetailDTO productDetail = productService.getProductDetail(id, lat, lon);

        ApiResponse<ProductDetailDTO> response = ApiResponse.<ProductDetailDTO>builder()
                .message("Product detail fetched successfully.")
                .data(productDetail)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * API nội bộ để kích hoạt đồng bộ cache giá từ Inventory Service.
     * Endpoint này nên được bảo mật và chỉ gọi bởi hệ thống nội bộ (ví dụ: một scheduled job).
     */
    @PostMapping("/internal/{id}/sync-price")
    public ResponseEntity<ApiResponse<Void>> syncProductPrice(@PathVariable Long id) {
        productService.syncPriceCache(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Price cache sync initiated for product " + id)
                .build();
        return ResponseEntity.ok(response);
    }

    // Các endpoint dành cho Admin (POST, PUT, DELETE) sẽ được đặt ở Admin Service.
    // Admin Service sẽ gọi các API nội bộ (internal) của Product Service nếu cần.
}