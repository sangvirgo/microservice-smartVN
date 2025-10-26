package com.smartvn.product_service.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ImageDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String downloadUrl;
    private Long productId;
    private LocalDateTime createdAt;
}