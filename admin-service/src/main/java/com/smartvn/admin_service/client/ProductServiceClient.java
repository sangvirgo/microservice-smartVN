package com.smartvn.admin_service.client;

import com.smartvn.admin_service.config.FeignClientConfig;
import com.smartvn.admin_service.dto.product.*;
import com.smartvn.admin_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Feign Client để giao tiếp với Product Service.
 */
@FeignClient(name = "product-service", configuration = FeignClientConfig.class, fallback = ProductServiceFallback.class)
public interface ProductServiceClient {

    /**
     * Lấy danh sách sản phẩm cho admin (bao gồm cả sản phẩm inactive).
     * Endpoint này cần được tạo trong Product Service.
     * @param page Số trang
     * @param size Kích thước trang
     * @param search Từ khóa tìm kiếm
     * @param categoryId Lọc theo category
     * @param isActive Lọc theo trạng thái active
     * @return Trang kết quả ProductAdminViewDTO
     */
    @GetMapping("${api.prefix}/internal/products/admin/all")
    ResponseEntity<ApiResponse<Page<ProductAdminViewDTO>>> getAllProductsAdmin(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "isActive", required = false) Boolean isActive);


    /**
     * Lấy chi tiết sản phẩm cho admin.
     * Có thể dùng endpoint public hoặc tạo endpoint internal riêng nếu cần thêm thông tin.
     * @param productId ID sản phẩm
     * @return ProductAdminViewDTO
     */
    @GetMapping("${api.prefix}/internal/products/admin/{productId}") // Hoặc dùng endpoint public nếu đủ thông tin
    ResponseEntity<ApiResponse<ProductDetailForEditDTO>> getProductDetailAdmin(@PathVariable("productId") Long productId);

    /**
     * Kích hoạt/Vô hiệu hóa sản phẩm.
     * Endpoint này cần được tạo trong Product Service.
     * @param productId ID sản phẩm
     * @return Phản hồi không có nội dung
     */
    @PutMapping("${api.prefix}/internal/products/admin/{productId}/toggle-active")
    ResponseEntity<ApiResponse<Void>> toggleProductActive(@PathVariable("productId") Long productId);

    /**
     * Xóa mềm sản phẩm (hoặc có thể là xóa cứng tùy logic).
     * Endpoint này cần được tạo trong Product Service.
     * @param productId ID sản phẩm
     * @return Phản hồi không có nội dung
     */
    @DeleteMapping("${api.prefix}/internal/products/admin/{productId}")
    ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable("productId") Long productId);

    /**
     * Cập nhật thông tin tồn kho cho một variant của sản phẩm.
     * Endpoint này cần được tạo trong Product Service.
     * @param productId ID sản phẩm
     * @param inventoryId ID của inventory record
     * @param request Dữ liệu cập nhật (số lượng, giá,...)
     * @return InventoryDTO đã được cập nhật
     */
    @PutMapping("${api.prefix}/internal/products/admin/{productId}/inventory/{inventoryId}")
    ResponseEntity<ApiResponse<InventoryDTO>> updateInventory(
            @PathVariable("productId") Long productId,
            @PathVariable("inventoryId") Long inventoryId,
            @RequestBody UpdateInventoryRequest request);

    /**
     * Thêm một inventory variant mới cho sản phẩm.
     * Endpoint này cần được tạo trong Product Service.
     * @param productId ID sản phẩm
     * @param request Dữ liệu inventory mới (size, số lượng, giá,...)
     * @return InventoryDTO mới được tạo
     */
    @PostMapping("${api.prefix}/internal/products/admin/{productId}/inventory")
    ResponseEntity<ApiResponse<InventoryDTO>> addInventory(
            @PathVariable("productId") Long productId,
            @RequestBody UpdateInventoryRequest request);

    // --- Endpoints liên quan đến Review Admin ---

    /**
     * Lấy danh sách review cần duyệt hoặc đã xử lý.
     * Endpoint này cần được tạo trong Product Service (vd: AdminReviewController).
     * @param page Số trang
     * @param size Kích thước trang
     * @param status Trạng thái review (PENDING, APPROVED, REJECTED, WARN)
     * @param productId Lọc theo sản phẩm (tùy chọn)
     * @param userId Lọc theo người dùng (tùy chọn)
     * @return Trang kết quả ReviewDTO
     */
    @GetMapping("${api.prefix}/internal/reviews/admin/all")
    ResponseEntity<ApiResponse<Page<ReviewDTO>>> getAllReviewsAdmin(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "userId", required = false) Long userId);
    /**
     * Xóa một review.
     * Endpoint này cần được tạo trong Product Service.
     * @param reviewId ID của review
     * @return Phản hồi không có nội dung
     */
    @DeleteMapping("${api.prefix}/internal/reviews/admin/{reviewId}")
    ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable("reviewId") Long reviewId);


    @GetMapping("${api.prefix}/internal/products/stats") // ✅ FIX: Sửa path
    ResponseEntity<ApiResponse<ProductStatsDTO>> getProductStats();

    /**
     * ✅ TẠO SINGLE PRODUCT
     */
    @PostMapping("${api.prefix}/internal/products/admin")
    ResponseEntity<ApiResponse<ProductAdminViewDTO>> createProduct(
            @RequestBody CreateProductRequest request);

    /**
     * ✅ TẠO BULK PRODUCTS
     */
    @PostMapping("${api.prefix}/internal/products/admin/bulk")
    ResponseEntity<ApiResponse<Map<String, Object>>> createBulkProducts(
            @RequestBody List<CreateProductRequest> requests);

    /**
     * ✅ CẬP NHẬT PRODUCT
     */
    @PutMapping("${api.prefix}/internal/products/admin/{productId}")
    ResponseEntity<ApiResponse<ProductAdminViewDTO>> updateProduct(
            @PathVariable("productId") Long productId,
            @RequestBody UpdateProductRequest request);

    /**
     * ✅ UPLOAD IMAGE
     * NOTE: Feign không hỗ trợ tốt MultipartFile,
     * có thể cần dùng cách khác (Base64 string hoặc URL)
     */
    @PostMapping(
            value = "${api.prefix}/internal/products/admin/{productId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<ApiResponse<ImageDTO>> uploadImage(
            @PathVariable("productId") Long productId,
            @RequestPart("file") MultipartFile file);

    /**
     * ✅ XÓA IMAGE
     */
    @DeleteMapping("${api.prefix}/internal/products/admin/images/{imageId}")
    ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable("imageId") Long imageId);
}