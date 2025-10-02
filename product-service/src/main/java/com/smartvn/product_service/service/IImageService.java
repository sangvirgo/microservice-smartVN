package com.smartvn.product_service.service.image;

import com.smartvn.product_service.dto.image.ImageDTO;
import com.smartvn.product_service.model.Image;
import com.smartvn.product_service.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IImageService {
    Image uploadImageForProduct(MultipartFile file, Product product) throws IOException;
    void deleteImage(Long imageId);
    public List<ImageDTO> getProductImages(Long productId);
}
