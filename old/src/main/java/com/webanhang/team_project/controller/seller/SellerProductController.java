package com.webanhang.team_project.controller.seller;

import com.webanhang.team_project.dto.product.CreateProductRequest;
import com.webanhang.team_project.dto.product.FilterProduct;
import com.webanhang.team_project.dto.product.ProductDTO;
import com.webanhang.team_project.dto.product.UpdateProductRequest;
import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.model.Product;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.repository.ProductRepository;
import com.webanhang.team_project.service.product.IProductService;
import com.webanhang.team_project.service.seller.ISellerProductService;
import com.webanhang.team_project.service.seller.SellerProductService;
import com.webanhang.team_project.service.user.IUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/seller/products")
public class SellerProductController {

    private final ISellerProductService sellerProductService;
    private final IUserService userService;
    private final ProductRepository productRepository;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createProduct(
            @RequestBody CreateProductRequest req,
            @RequestHeader("Authorization") String jwt) {

        User seller = userService.findUserByJwt(jwt);
        req.setSellerId(seller.getId());
        ProductDTO productDto = sellerProductService.createProduct(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(productDto, "Tạo sản phẩm thành công"));
    }

    @PutMapping("/{productId}/update")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody UpdateProductRequest updatedProduct,
            @RequestHeader("Authorization") String jwt) {

        ProductDTO productDto = sellerProductService.updateProduct(productId, updatedProduct);

        return ResponseEntity.ok(ApiResponse.success(productDto, "Cập nhật sản phẩm thành công"));
    }

    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<ApiResponse> deleteProduct(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long productId) {

        try {
            User seller = userService.findUserByJwt(jwt);

            // Verify product belongs to seller
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            if (!product.getSellerId().equals(seller.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only delete your own products"));
            }

            sellerProductService.deleteProduct(productId);
            return ResponseEntity.ok(ApiResponse.success(null, "Xóa sản phẩm thành công"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/list-products")
    public ResponseEntity<ApiResponse> getSellerProducts(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            // Search and filter parameters matching FilterProduct
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String topLevelCategory,
            @RequestParam(required = false) String secondLevelCategory,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status // Additional filter for stock status
    ) {
        User seller = userService.findUserByJwt(jwt);

        Sort sortObj = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // Create filter object using existing FilterProduct
        FilterProduct filter = new FilterProduct();
        filter.setKeyword(keyword);
        filter.setTopLevelCategory(topLevelCategory);
        filter.setSecondLevelCategory(secondLevelCategory);
        filter.setColor(color);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setSort(sort);

        Page<ProductDTO> productPage = sellerProductService.getSellerProductsWithFilter(
                seller.getId(), pageable, filter, status);

        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("pagination", Map.of(
                "currentPage", productPage.getNumber(),
                "pageSize", productPage.getSize(),
                "totalElements", productPage.getTotalElements(),
                "totalPages", productPage.getTotalPages(),
                "hasNext", productPage.hasNext(),
                "hasPrevious", productPage.hasPrevious(),
                "isFirst", productPage.isFirst(),
                "isLast", productPage.isLast()
        ));

        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách sản phẩm thành công"));
    }

    // Get filter statistics
    @GetMapping("/filter-stats")
    public ResponseEntity<ApiResponse> getFilterStatistics(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        Map<String, Object> stats = sellerProductService.getFilterStatistics(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê bộ lọc thành công"));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductDetails(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long productId) {

        ProductDTO productDTO = sellerProductService.getProductDetail(productId);

        return ResponseEntity.ok(ApiResponse.success(productDTO, "Lấy chi tiết sản phẩm thành công"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getProductStats(@RequestHeader("Authorization") String jwt) {

        User seller = userService.findUserByJwt(jwt);
        Map<String, Object> stats = sellerProductService.getProductStatOfSeller(seller.getId());

        return ResponseEntity
                .ok(ApiResponse.success(stats, "Lấy thống kê sản phẩm thành công"));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse> getSellerCategories(@RequestHeader("Authorization") String jwt) {
        User seller = userService.findUserByJwt(jwt);
        Map<String, Object> categories = sellerProductService.getSellerCategories(seller.getId());
        return ResponseEntity.ok(ApiResponse.success(categories, "Lấy danh mục thành công"));
    }

    @PostMapping("/create-multi-product")
    public ResponseEntity<ApiResponse> createMultipleProduct(
            @RequestBody List<CreateProductRequest> requests,
            @RequestHeader("Authorization") String jwt) {

        List<ProductDTO> dtos = sellerProductService.createMultipleProducts(requests);

        return ResponseEntity
                .ok(ApiResponse.success(dtos, "Tạo nhiều sản phẩm thành công"));
    }
}
