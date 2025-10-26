package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.dto.product.InventoryDTO;
import com.smartvn.admin_service.dto.product.ProductAdminViewDTO;
import com.smartvn.admin_service.dto.product.UpdateInventoryRequest;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.exceptions.BaseAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminProductService extends BaseAdminService {
    private final ProductServiceClient productServiceClient;

    public Page<ProductAdminViewDTO> getAllProducts(int page, int size,
                                                    String search, Long categoryId, Boolean isActive) {
        ResponseEntity<ApiResponse<Page<ProductAdminViewDTO>>> response =
                productServiceClient.getAllProductsAdmin(page, size, search, categoryId, isActive);
        return handleResponse(response, "Failed to get products");
    }

    public Page<ProductAdminViewDTO> getProductById(Long productId) {
        ResponseEntity<ApiResponse<Page<ProductAdminViewDTO>>> rs = productServiceClient.
    }

    public void toggleActive(Long productId) {
        ResponseEntity<ApiResponse<Void>> response =
                productServiceClient.toggleProductActive(productId);
        handleVoidResponse(response, "Failed to toggle product");
    }

    public void deleteProduct(Long productId) {
        ResponseEntity<ApiResponse<Void>> response =
                productServiceClient.deleteProduct(productId);
        handleVoidResponse(response, "Failed to delete product");
    }

    public InventoryDTO updateInventory(Long productId, Long inventoryId,
                                        UpdateInventoryRequest request) {
        ResponseEntity<ApiResponse<InventoryDTO>> response =
                productServiceClient.updateInventory(productId, inventoryId, request);
        return handleResponse(response, "Failed to update inventory");
    }

    public InventoryDTO addInventory(Long productId, UpdateInventoryRequest request) {
        ResponseEntity<ApiResponse<InventoryDTO>> response =
                productServiceClient.addInventory(productId, request);
        return handleResponse(response, "Failed to add inventory");
    }

}
