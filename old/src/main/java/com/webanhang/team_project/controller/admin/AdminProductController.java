package com.webanhang.team_project.controller.admin;

import com.webanhang.team_project.dto.product.CreateProductRequest;
import com.webanhang.team_project.dto.product.FilterProduct;
import com.webanhang.team_project.dto.product.ProductDTO;
import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.model.Product;
import com.webanhang.team_project.repository.CategoryRepository;
import com.webanhang.team_project.repository.ProductRepository;
import com.webanhang.team_project.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin/products")
public class AdminProductController {

    private final IProductService productService;
    private final CategoryRepository categoryRepository;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createProduct(@RequestBody CreateProductRequest request) {
        Product product = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Tạo sản phẩm thành công"));
    }


    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId) {
        productService.adminDeleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(null, String.format("Delete product have ID %d successfully", productId)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> findAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String topLevelCategory,
            @RequestParam(required = false) String secondLevelCategory,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status
    ) {
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

        Page<ProductDTO> productPage = productService.getProductsWithFilter(pageable, filter, status);
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

        return ResponseEntity.ok(ApiResponse.success(response, "Lấy tất cả sản phẩm thành công"));
    }

    @PutMapping("/{productId}/update")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long productId, @RequestBody Product product) {
        ProductDTO updatedProduct = productService.updateProductByID(productId, product);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Cập nhật sản phẩm thành công"));
    }

    @PostMapping("/create-multiple")
    public ResponseEntity<ApiResponse> createMultipleProducts(@RequestBody CreateProductRequest[] requests) {
        for (CreateProductRequest request : requests) {
            productService.createProduct(request);
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Tạo nhiều sản phẩm thành công"));
    }

    @GetMapping("/top-selling")
    public ResponseEntity<ApiResponse> getTopSellingProducts(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> topProducts = productService.getTopSellingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(topProducts, "Get top selling products success"));
    }

    @GetMapping("/revenue-by-category")
    public ResponseEntity<ApiResponse> getRevenueByCateogry() {
        Map<String, Object> categoryRevenue = productService.getRevenueByCateogry();
        return ResponseEntity.ok(ApiResponse.success(categoryRevenue, "Get revenue by category success"));
    }

    @GetMapping("/filter-stats")
    public ResponseEntity<ApiResponse> getFilterStatistics() {
        Map<String, Object> stats = productService.getAdminFilterStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê bộ lọc thành công"));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse> getAllCategories() {
        Map<String, Object> categories = productService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories, "Lấy danh mục thành công"));
    }
}