package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.ProductListingDTO;
import com.smartvn.product_service.dto.admin.CreateProductRequest;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Image;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.service.ImageService;
import com.smartvn.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ImageService  imageService;
    /**
     * API để lấy danh sách sản phẩm (phân trang) và hỗ trợ tìm kiếm, lọc.
     *
     * @param keyword Từ khóa tìm kiếm trong title
     * @param topLevelCategory Tên category cấp 1 (vd: "Điện thoại", "Laptop")
     * @param secondLevelCategory Tên category cấp 2 (vd: "iPhone", "Samsung")
     * @param minPrice Giá tối thiểu
     * @param maxPrice Giá tối đa
     * @param pageable Thông tin phân trang
     *
     * Example: GET /api/v1/products?topLevelCategory=Laptop&secondLevelCategory=MacBook&minPrice=20000000
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductListingDTO>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String topLevelCategory,
            @RequestParam(required = false) String secondLevelCategory,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {

        log.info("🔍 Search request - keyword: {}, topLevel: {}, secondLevel: {}, price: {}-{}",
                keyword, topLevelCategory, secondLevelCategory, minPrice, maxPrice);

        Page<ProductListingDTO> productPage = productService.searchProducts(
                keyword, topLevelCategory, secondLevelCategory, minPrice, maxPrice, pageable
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
     * Kiểm tra user đã mua sản phẩm chưa
     */
    @GetMapping("/{id}/check-purchased")
    public ResponseEntity<ApiResponse<Boolean>> checkUserPurchased(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success(false, "User not logged in"));
        }

        boolean hasPurchased = productService.hasUserPurchasedProduct(userId, id);
        return ResponseEntity.ok(ApiResponse.success(hasPurchased, "Purchase status checked"));
    }
}