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
     * API để nhập hàng loạt sản phẩm
     * TODO: Thêm @PreAuthorize("hasRole('ADMIN')") khi deploy production
     */
    @PostMapping("/create-multiple")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkImportProducts(
            @RequestBody List<CreateProductRequest> requests) {

        log.info("📦 Bulk import {} products", requests.size());

        ProductService.BulkImportResult result =
                productService.createBulkProductsOptimized(requests);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRequested", requests.size());
        summary.put("successCount", result.getSuccessProducts().size());
        summary.put("failureCount", result.getFailures().size());
        summary.put("successProducts", result.getSuccessProducts());
        summary.put("failures", result.getFailures());

        return ResponseEntity.ok(
                ApiResponse.success(summary,
                        String.format("Imported %d/%d products",
                                result.getSuccessProducts().size(),
                                requests.size()))
        );
    }

    /**
     * SỬA ĐỔI: Chuyển endpoint upload ảnh vào đây
     * API để upload hình ảnh cho một sản phẩm.
     * Cần được bảo vệ (chỉ ADMIN).
     */
    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<Image>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        Image savedImage = imageService.uploadImageForProduct(id, file);

        ApiResponse<Image> response = ApiResponse.<Image>builder()
                .message("Image uploaded successfully.")
                .data(savedImage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}