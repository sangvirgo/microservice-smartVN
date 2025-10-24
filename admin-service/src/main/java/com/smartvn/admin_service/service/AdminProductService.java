package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.dto.product.InventoryDTO;
import com.smartvn.admin_service.dto.product.ProductAdminViewDTO;
import com.smartvn.admin_service.dto.product.UpdateInventoryRequest;
import com.smartvn.admin_service.dto.response.ApiResponse;
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
public class AdminProductService {
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
     * Hàm tiện ích xử lý response từ Feign Client.
     * @param response ResponseEntity từ Feign client
     * @param errorMessage Thông báo lỗi nếu request thất bại
     * @return Dữ liệu từ response nếu thành công
     * @throws ResponseStatusException Nếu request thất bại hoặc không có dữ liệu
     */
    private <T> T handleResponse(ResponseEntity<ApiResponse<T>> response, String errorMessage) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getData() != null) {
            return response.getBody().getData();
        } else {
            HttpStatus status = (HttpStatus) response.getStatusCode();
            String message = (response.getBody() != null && response.getBody().getMessage() != null)
                    ? response.getBody().getMessage()
                    : errorMessage;
            log.error("{} - Status: {}, Message: {}", errorMessage, status, message);
            // Ném lỗi để GlobalExceptionHandler có thể bắt và trả về response chuẩn
            throw new ResponseStatusException(status, message);
        }
    }
    // Overload cho trường hợp response không có data (Void)
    private void handleVoidResponse(ResponseEntity<ApiResponse<Void>> response, String errorMessage) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            HttpStatus status = (HttpStatus) response.getStatusCode(); // Cast an toàn hơn
            String message = (response.getBody() != null && response.getBody().getMessage() != null)
                    ? response.getBody().getMessage()
                    : errorMessage;
            log.error("{} - Status: {}, Message: {}", errorMessage, status, message);
            throw new ResponseStatusException(status, message);
        }
        // Thành công thì không cần làm gì
    }
}
