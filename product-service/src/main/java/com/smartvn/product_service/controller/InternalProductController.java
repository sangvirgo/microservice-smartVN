package com.smartvn.product_service.controller;


import com.smartvn.product_service.dto.BulkProductRequest;
import com.smartvn.product_service.dto.InventoryCheckRequest;
import com.smartvn.product_service.dto.ProductDTO;
import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.admin.ReviewAdminDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Inventory;
import com.smartvn.product_service.model.Review;
import com.smartvn.product_service.service.InventoryService;
import com.smartvn.product_service.service.ProductService;
import com.smartvn.product_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${api.prefix}/internal")
public class InternalProductController {
    private final InventoryService  inventoryService;
    private final ProductService productService;
    private final ReviewService  reviewService;

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        ProductDetailDTO dto = productService.getProductDetail(productId);
        return ResponseEntity.ok(dto.toSimpleDTO());
    }

    @GetMapping("/products/{productId}/inventory")
    public ResponseEntity<List<BulkProductRequest.InventoryItemDTO>> getInventory(@PathVariable Long productId) {
        List<Inventory> invs = inventoryService.getInventoriesByProduct(productId);
        List<BulkProductRequest.InventoryItemDTO> dtos = invs.stream()
                .map(BulkProductRequest.InventoryItemDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/inventory/check")
    public ResponseEntity<Boolean> checkInventoryAvailability(
            @RequestBody InventoryCheckRequest request) {
        boolean isValid = inventoryService.checkInventoryAvailability(request);

        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/inventory/reduce")
    public ResponseEntity<Void> reduceInventory(
            @RequestBody InventoryCheckRequest request) {

        inventoryService.batchReduceOneInventory(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inventory/restore")
    public ResponseEntity<Void> restoreInventory(
            @RequestBody InventoryCheckRequest request) {

        inventoryService.batchRetoreOneInventory(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inventory/batch-check")
    public ResponseEntity<Map<String, Boolean>> batchCheckInventory(
            @RequestBody List<InventoryCheckRequest> requests) {

        Map<String, Boolean> results = new HashMap<>();

        for (InventoryCheckRequest req : requests) {
            String key = req.getProductId() + "-" + req.getSize();
            boolean hasStock = inventoryService.checkInventoryAvailability(req);
            results.put(key, hasStock);
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/inventory/batch-reduce")
    public ResponseEntity<Void> batchReduceInventory(
            @RequestBody List<InventoryCheckRequest> requests) {

        inventoryService.batchReduceInventory(requests);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{productId}/increase-sold")
    public ResponseEntity<Void> increaseQuantitySold(@RequestBody InventoryCheckRequest request) {

        productService.increaseQuantitySold(request);
        return ResponseEntity.ok().build();
    }

    /**
     * ✅ XÓA REVIEW
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted"));
    }

    /**
     * ✅ LẤY TẤT CẢ REVIEWS CHO ADMIN
     */
    @GetMapping("/reviews/admin/all")
    public ResponseEntity<ApiResponse<Page<ReviewAdminDTO>>> getAllReviewsAdmin(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "userId", required = false) Long userId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewService.searchReviewsForAdmin(status, productId, userId, pageable);
        Page<ReviewAdminDTO> dtos = reviews.map(this::convertToAdminDTO);

        return ResponseEntity.ok(ApiResponse.success(dtos, "Reviews retrieved"));
    }
}
