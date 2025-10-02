package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.image.ImageDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Image;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.service.image.CloudinaryService;
import com.smartvn.product_service.service.image.ImageService;
import com.smartvn.product_service.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {
    private final ImageService imageService;
    private final CloudinaryService cloudinaryService;
    private final ProductService productService;

    @PostMapping("/upload/{productId}")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long productId,
            @RequestParam("image") MultipartFile file) throws IOException {

        log.info("Nhận yêu cầu tải lên hình ảnh cho sản phẩm ID: {}", productId);

        Product product = productService.findProductById(productId);
        Image image = imageService.uploadImageForProduct(file, product);

        log.info("Tải lên hình ảnh thành công, ID: {}", image.getId());

        Map<String, Object> data = Map.of(
                "imageId", image.getId(),
                "url", image.getDownloadUrl()
        );

        return ResponseEntity.ok(ApiResponse.success(data, "Tải lên hình ảnh thành công"));
    }

    @DeleteMapping("/delete/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            log.info("Nhận yêu cầu xóa hình ảnh ID: {}", imageId);
            imageService.deleteImage(imageId);
            log.info("Xóa hình ảnh thành công, ID: {}", imageId);

            return ResponseEntity.ok(ApiResponse.success(null, "Xóa hình ảnh thành công"));
        } catch (RuntimeException e) {
            log.error("Lỗi khi xóa hình ảnh ID {}: {}", imageId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy hình ảnh: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi không xác định khi xóa hình ảnh ID {}: {}", imageId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể xóa hình ảnh: " + e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse> getProductImages(@PathVariable Long productId) {
        productService.findProductById(productId);
        List<ImageDTO> imagesDTO = imageService.getProductImages(productId);

        log.info("Tìm thấy {} hình ảnh cho sản phẩm ID: {}", imagesDTO.size(), productId);

        return ResponseEntity.ok(ApiResponse.success(imagesDTO, "Lấy danh sách hình ảnh thành công"));
    }
}