package com.webanhang.team_project.service.image;

import com.webanhang.team_project.dto.image.ImageDTO;
import com.webanhang.team_project.model.Image;
import com.webanhang.team_project.model.Product;
import com.webanhang.team_project.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService implements IImageService{
    private final ImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public Image uploadImageForProduct(MultipartFile file, Product product) throws IOException {
        try {
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);

            Image image = new Image();
            image.setProduct(product);
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setDownloadUrl((String) uploadResult.get("url"));

            return imageRepository.save(image);
        } catch (IOException e) {
            log.error("Lỗi khi tải hình ảnh lên cho sản phẩm: {}", product.getId(), e);
            throw new IOException("Không thể tải lên hình ảnh: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Hình ảnh không tồn tại"));

        // Xóa khỏi Cloudinary
        String publicId = cloudinaryService.extractPublicIdFromUrl(image.getDownloadUrl());
        if (publicId != null) {
            try {
                cloudinaryService.deleteImage(publicId);
            } catch (Exception e) {
                log.error("Không thể xóa hình ảnh từ Cloudinary: {}", publicId, e);
            }
        }
        imageRepository.delete(image);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ImageDTO> getProductImages(Long productId) {
        List<Image> images = imageRepository.findByProductId(productId);
        List<ImageDTO> imagesDTO = images.stream()
                .map(ImageDTO::new)
                .toList();
        return imagesDTO;
    }

    @Transactional
    public void deleteAllProductImages(Long productId) {
        List<Image> images = imageRepository.findByProductId(productId);

        // Xóa từng ảnh trên Cloudinary
        for (Image image : images) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(image.getDownloadUrl());
            if (publicId != null) {
                try {
                    cloudinaryService.deleteImage(publicId);
                } catch (Exception e) {
                    // Log lỗi nhưng vẫn tiếp tục xóa các ảnh khác
                    log.error("Không thể xóa hình ảnh từ Cloudinary: {}", publicId, e);
                }
            }
        }

        // Xóa tất cả ảnh từ database
        imageRepository.deleteByProductId(productId);
        log.info("Đã xóa thành công tất cả hình ảnh của sản phẩm {}", productId);
    }
}
