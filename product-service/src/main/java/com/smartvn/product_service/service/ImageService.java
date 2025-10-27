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
        log.info("--- Starting deleteImage process for ID: {} ---", imageId); // Log bắt đầu

        // 1. Tìm ảnh trong DB
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> {
                    log.error("Image not found in database with id: {}", imageId);
                    return new AppException("Image not found with id: " + imageId, HttpStatus.NOT_FOUND);
                });
        log.debug("Found image record: {}", image);

        // 2. Lấy giá trị từ trường fileName, giả định đây là public_id
        String potentialPublicId = image.getFileName();
        log.debug("Potential Cloudinary public_id from fileName: '{}'", potentialPublicId);

        // 3. Kiểm tra xem potentialPublicId có vẻ là public_id hợp lệ không
        boolean looksLikePublicId = potentialPublicId != null && !potentialPublicId.trim().isEmpty() && potentialPublicId.contains("/");

        if (looksLikePublicId) {
            // 4. Nếu có vẻ là public_id, thử xóa trên Cloudinary
            try {
                log.info("Attempting to delete image from Cloudinary using fileName as public_id: {}", potentialPublicId);
                cloudinaryService.delete(potentialPublicId); // Gọi phương thức void delete()

                // Nếu không có exception, ghi log thành công (không có kết quả chi tiết từ hàm void)
                log.info("Cloudinary delete API call executed successfully (or image not found on Cloudinary) for public_id: {}", potentialPublicId);

            } catch (IOException e) {
                // Lỗi giao tiếp mạng với Cloudinary
                log.error("!!! IOException while trying to delete image {} (public_id from fileName: '{}') from Cloudinary: {}",
                        imageId, potentialPublicId, e.getMessage(), e); // Log cả stack trace
                // Ném lỗi để báo hiệu sự cố và rollback transaction DB.
                throw new AppException("Failed to communicate with Cloudinary to delete image. Database changes rolled back.", HttpStatus.INTERNAL_SERVER_ERROR, e);
            } catch (RuntimeException e) { // Bắt các lỗi Runtime khác có thể xảy ra trong cloudinaryService.delete
                log.error("!!! RuntimeException during Cloudinary deletion attempt for image id {} (public_id from fileName: '{}'): {}", imageId, potentialPublicId, e.getMessage(), e); // Log cả stack trace
                // Ném lỗi để rollback
                throw new AppException("An unexpected error occurred during Cloudinary deletion attempt", HttpStatus.INTERNAL_SERVER_ERROR, e);
            }
        } else {
            // 5. Nếu fileName không giống public_id, ghi log cảnh báo
            log.warn("fileName '{}' for image id {} does not look like a Cloudinary public_id. Skipping Cloudinary deletion.", potentialPublicId, imageId);
        }

        // 6. Xóa bản ghi khỏi cơ sở dữ liệu
        try {
            log.info("Attempting to delete image record from database with id: {}", imageId);
            imageRepository.delete(image);
            log.info("--- Successfully deleted image record from database with id: {} ---", imageId); // Log kết thúc thành công
        } catch (Exception e) {
            log.error("!!! Failed to delete image record {} from database: {}", imageId, e.getMessage(), e); // Log cả stack trace
            // Ném lỗi nếu xóa DB thất bại
            throw new AppException("Failed to delete image record from database", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}