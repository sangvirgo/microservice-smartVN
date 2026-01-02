package com.smartvn.product_service.controller;

import com.smartvn.product_service.client.RecommendationServiceClient;
import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.ProductListingDTO;
import com.smartvn.product_service.dto.admin.CreateProductRequest;
import com.smartvn.product_service.dto.ai.HomepageRecommendDTO;
import com.smartvn.product_service.dto.ai.SimilarRecommendDTO;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ImageService imageService;
    private final RecommendationServiceClient recommendationClient;

    /**
     * API để lấy danh sách sản phẩm (phân trang) và hỗ trợ tìm kiếm, lọc.
     *
     * @param keyword             Từ khóa tìm kiếm trong title
     * @param topLevelCategory    Tên category cấp 1 (vd: "Điện thoại", "Laptop")
     * @param secondLevelCategory Tên category cấp 2 (vd: "iPhone", "Samsung")
     * @param minPrice            Giá tối thiểu
     * @param maxPrice            Giá tối đa
     * @param pageable            Thông tin phân trang
     *                            <p>
     *                            Example: GET /api/v1/products?topLevelCategory=Laptop&secondLevelCategory=MacBook&minPrice=20000000
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

    private Long toLongOrNull(Long value) {
        return value;
    }
    /**
     * API để lấy thông tin chi tiết của một sản phẩm.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductDetail(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("📦 Product detail request - productId: {}, userId: {}", id, userId);

        ProductDetailDTO productDetail = productService.getProductDetail(id);

        // ✅ GỌI AI ĐỂ LẤY SIMILAR PRODUCTS
        try {
            SimilarRecommendDTO aiResponse = recommendationClient
                    .getProductDetailRecommendations(id.toString(), userId, 10);

            log.info("✅ Similar products: strategy={}, count={}",
                    aiResponse.getStrategy(), aiResponse.getCount());

            // ✅ CONVERT IDs → FULL ProductListingDTO (thay vì chỉ IDs)
            List<ProductListingDTO> similarProducts = aiResponse.getProduct_ids().stream()
                    .map(productId -> {
                        try {
                            Product p = productService.findById(Long.parseLong(productId));
                            return productService.toListingDTO(p);
                        } catch (Exception e) {
                            log.warn("⚠️ Similar product {} not found", productId);
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            // ✅ GÁN VÀO RESPONSE (cần thêm field similarProducts trong DTO)
            productDetail.setSimilarProducts(similarProducts);

        } catch (Exception e) {
            log.warn("⚠️ Failed to get similar products: {}", e.getMessage());
            // Không throw - tiếp tục trả product detail
        }

        return ResponseEntity.ok(ApiResponse.success(productDetail, "Product detail"));
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


    @GetMapping("/recommendations/homepage")
    public ResponseEntity<ApiResponse<List<ProductListingDTO>>> getHomepageRecommendations(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("🎯 Homepage recommendations request - userId: {}", userId);

        try {
            // ✅ GỌI AI SERVICE (X-API-KEY tự động inject)
            HomepageRecommendDTO aiResponse = recommendationClient
                    .getHomepageRecommendations(userId, 10);

            log.info("✅ AI Response: strategy={}, count={}, ids={}",
                    aiResponse.getStrategy(),
                    aiResponse.getCount(),
                    aiResponse.getProduct_ids());

            // ✅ CONVERT PRODUCT IDs → FULL ProductListingDTO
            List<ProductListingDTO> products = aiResponse.getProduct_ids().stream()
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("⚠️ Invalid product ID: {}", id);
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)  // Remove nulls
                    .map(productId -> {
                        try {
                            Product product = productService.findById(productId);
                            return productService.toListingDTO(product);
                        } catch (Exception e) {
                            log.warn("⚠️ Product {} not found", productId);
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)  // Remove nulls
                    .collect(Collectors.toList());

            log.info("✅ Returning {} products to FE", products.size());

            return ResponseEntity.ok(ApiResponse.success(
                    products,
                    "Recommendations (strategy: " + aiResponse.getStrategy() + ")"
            ));

        } catch (Exception e) {
            log.error("❌ Failed to get homepage recommendations", e);

            // ✅ FALLBACK: Return empty list
            return ResponseEntity.ok(ApiResponse.success(
                    Collections.emptyList(),
                    "AI service unavailable"
            ));
        }
    }



}