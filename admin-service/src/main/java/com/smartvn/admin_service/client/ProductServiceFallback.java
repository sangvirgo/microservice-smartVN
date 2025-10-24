package com.smartvn.admin_service.client;

import com.smartvn.admin_service.dto.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import com.smartvn.admin_service.dto.product.ProductAdminViewDTO;
import com.smartvn.admin_service.dto.product.InventoryDTO;
import com.smartvn.admin_service.dto.product.UpdateInventoryRequest;

public class ProductServiceFallback implements ProductServiceClient {
    @Override
    public ResponseEntity<ApiResponse<Page<ProductAdminViewDTO>>> getAllProductsAdmin(int page, int size, String search, Long categoryId, Boolean isActive) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<ProductAdminViewDTO>> getProductDetailAdmin(Long productId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> toggleProductActive(Long productId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteProduct(Long productId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<InventoryDTO>> updateInventory(Long productId, Long inventoryId, UpdateInventoryRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<InventoryDTO>> addInventory(Long productId, UpdateInventoryRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Page<?>>> getAllReviewsAdmin(int page, int size, String status, Long productId, Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> updateReviewStatus(Long reviewId, String newStatus) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteReview(Long reviewId) {
        return null;
    }
}
