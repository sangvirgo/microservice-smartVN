package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.dto.product.*;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.exceptions.BaseAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    /**
     * ✅ LẤY CHI TIẾT PRODUCT BY ID
     */
    public ProductDetailForEditDTO getProductById(Long productId) {
        log.info("📦 Getting product detail: {}", productId);

        ResponseEntity<ApiResponse<ProductDetailForEditDTO>> response =
                productServiceClient.getProductDetailAdmin(productId);

        return handleResponse(response, "Failed to get product detail");
    }

    /**
     * ✅ TẠO SINGLE PRODUCT
     */
    public ProductAdminViewDTO createProduct(CreateProductRequest request) {
        log.info("📦 Creating product: {}", request.getTitle());

        ResponseEntity<ApiResponse<ProductAdminViewDTO>> response =
                productServiceClient.createProduct(request);

        return handleResponse(response, "Failed to create product");
    }

    /**
     * ✅ TẠO BULK PRODUCTS
     */
    public Map<String, Object> createBulkProducts(List<CreateProductRequest> requests) {
        log.info("📦 Bulk creating {} products", requests.size());

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                productServiceClient.createBulkProducts(requests);

        return handleResponse(response, "Failed to bulk create products");
    }

    /**
     * ✅ CẬP NHẬT PRODUCT
     */
    public ProductAdminViewDTO updateProduct(Long productId, UpdateProductRequest request) {
        log.info("📝 Updating product: {}", productId);

        ResponseEntity<ApiResponse<ProductAdminViewDTO>> response =
                productServiceClient.updateProduct(productId, request);

        return handleResponse(response, "Failed to update product");
    }

    /**
     * ✅ UPLOAD IMAGE
     */
    public String uploadImage(Long productId, MultipartFile file) {
        log.info("📸 Uploading image for product: {}", productId);

        ResponseEntity<ApiResponse<ImageDTO>> response =
                productServiceClient.uploadImage(productId, file);

        ImageDTO image = handleResponse(response, "Failed to upload image");
        return image.getDownloadUrl();
    }

    /**
     * ✅ XÓA IMAGE
     */
    public void deleteImage(Long imageId) {
        log.info("🗑️ Deleting image: {}", imageId);

        ResponseEntity<ApiResponse<Void>> response =
                productServiceClient.deleteImage(imageId);

        handleVoidResponse(response, "Failed to delete image");
    }



}
