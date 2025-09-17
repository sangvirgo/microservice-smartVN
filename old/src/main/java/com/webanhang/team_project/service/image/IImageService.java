package com.webanhang.team_project.service.image;

import com.webanhang.team_project.dto.image.ImageDTO;
import com.webanhang.team_project.model.Image;
import com.webanhang.team_project.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IImageService {
    Image uploadImageForProduct(MultipartFile file, Product product) throws IOException;
    void deleteImage(Long imageId);
    public List<ImageDTO> getProductImages(Long productId);
}
