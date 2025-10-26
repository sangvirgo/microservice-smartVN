package com.smartvn.admin_service.client;

import com.smartvn.admin_service.dto.product.*;
import com.smartvn.admin_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
public class ProductServiceFallback implements ProductServiceClient {
    @Override
    public ResponseEntity<ApiResponse<Page<ProductAdminViewDTO>>> getAllProductsAdmin(int page, int size, String search, Long categoryId, Boolean isActive) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailForEditDTO>> getProductDetailAdmin(Long productId) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> toggleProductActive(Long productId) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteProduct(Long productId) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<InventoryDTO>> updateInventory(Long productId, Long inventoryId, UpdateInventoryRequest request) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<InventoryDTO>> addInventory(Long productId, UpdateInventoryRequest request) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getAllReviewsAdmin(int page, int size, String status, Long productId, Long userId) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }


    @Override
    public ResponseEntity<ApiResponse<Void>> deleteReview(Long reviewId) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductStatsDTO>> getProductStats() {
        log.error("Product Service unavailable. Returning zero stats.");

        ProductStatsDTO emptyStats = new ProductStatsDTO();
        emptyStats.setTotalProducts(0L);
        emptyStats.setActiveProducts(0L);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.success(
                        emptyStats,
                        "Service unavailable"
                ));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductAdminViewDTO>> createProduct(CreateProductRequest request) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<Map<String, Object>>> createBulkProducts(List<CreateProductRequest> requests) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductAdminViewDTO>> updateProduct(Long productId, UpdateProductRequest request) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<ImageDTO>> uploadImage(Long productId, MultipartFile file) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteImage(Long imageId) {
        log.error("Product Service unavailable. Returning empty result.");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product service đang bảo trì. Vui lòng thử lại sau."));
    }
}
