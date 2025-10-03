package com.smartvn.product_service.service;

import com.smartvn.product_service.exceptions.AppException;
import com.smartvn.product_service.model.Image;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.repository.ImageRepository;
import com.smartvn.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Tải lên một hình ảnh mới và liên kết nó với một sản phẩm.
     *
     * @param productId ID của sản phẩm.
     * @param file      File hình ảnh.
     * @return Entity Image đã được lưu.
     */
    @Transactional
    public Image uploadImageForProduct(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Product not found with id: " + productId, HttpStatus.NOT_FOUND));

        try {
            Map<String, String> uploadResult = cloudinaryService.upload(file);

            Image image = new Image();
            image.setFileName(uploadResult.get("public_id")); // Lưu public_id để xóa sau này
            image.setFileType(file.getContentType());
            image.setDownloadUrl(uploadResult.get("url"));
            image.setProduct(product);

            log.info("Image uploaded for product {}: {}", productId, image.getDownloadUrl());
            return imageRepository.save(image);

        } catch (IOException e) {
            log.error("Failed to upload image for product {}: {}", productId, e.getMessage());
            throw new AppException("Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Xóa một hình ảnh dựa trên ID của nó.
     *
     * @param imageId ID của hình ảnh cần xóa.
     */
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new AppException("Image not found with id: " + imageId, HttpStatus.NOT_FOUND));

        try {
            cloudinaryService.delete(image.getFileName()); // Dùng public_id (lưu trong fileName) để xóa
            imageRepository.delete(image);
            log.info("Successfully deleted image with id: {}", imageId);
        } catch (IOException e) {
            log.error("Failed to delete image {} from Cloudinary: {}", imageId, e.getMessage());
            throw new AppException("Failed to delete image from storage", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Xóa tất cả hình ảnh liên quan đến một sản phẩm.
     * Thường được gọi khi sản phẩm bị xóa.
     *
     * @param productId ID của sản phẩm.
     */
    @Transactional
    public void deleteAllProductImages(Long productId) {
        List<Image> images = imageRepository.findByProductId(productId);
        if (images.isEmpty()) {
            return;
        }

        for (Image image : images) {
            try {
                cloudinaryService.delete(image.getFileName());
            } catch (IOException e) {
                // Ghi log lỗi nhưng vẫn tiếp tục để xóa các ảnh khác
                log.error("Failed to delete image {} (public_id: {}) from Cloudinary for product {}: {}",
                        image.getId(), image.getFileName(), productId, e.getMessage());
            }
        }
        imageRepository.deleteAll(images);
        log.info("Deleted all {} images for product {}", images.size(), productId);
    }
}