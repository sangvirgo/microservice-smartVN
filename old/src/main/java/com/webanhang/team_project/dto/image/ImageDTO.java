package com.webanhang.team_project.dto.image;

import com.webanhang.team_project.model.Image;
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