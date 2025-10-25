package com.smartvn.product_service.controller;

import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.model.Image;
import com.smartvn.product_service.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.prefix}/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * API để upload hình ảnh cho một sản phẩm.
     * Cần được bảo vệ (chỉ ADMIN).
     */
    @PostMapping("/products/{productId}/images")
    public ResponseEntity<ApiResponse<Image>> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        Image savedImage = imageService.uploadImageForProduct(productId, file);

        ApiResponse<Image> response = ApiResponse.<Image>builder()
                .message("Image uploaded successfully.")
                .data(savedImage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * API để xóa một hình ảnh.
     * Cần được bảo vệ (chỉ ADMIN).
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {
        imageService.deleteImage(imageId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Image with id " + imageId + " deleted successfully.")
                .build();
        return ResponseEntity.ok(response);
    }
}