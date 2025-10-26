package com.smartvn.admin_service.dto.product;

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