package com.smartvn.product_service.dto;

import com.smartvn.product_service.model.Image;
import lombok.Data;

@Data
public class ImageDTO {
    private String fileName;
    private String downloadUrl;

    public ImageDTO(Image image) {
        this.fileName = image.getFileName();
        this.downloadUrl = image.getDownloadUrl();
    }
}