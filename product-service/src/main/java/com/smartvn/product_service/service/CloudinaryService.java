package com.smartvn.product_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Tải file lên Cloudinary.
     *
     * @param file File cần tải lên.
     * @return Một Map chứa 'url' và 'public_id' của file đã tải lên.
     * @throws IOException Nếu có lỗi trong quá trình tải file.
     */
    public Map<String, String> upload(MultipartFile file) throws IOException {
        // Tạo một public_id ngẫu nhiên và duy nhất cho file ảnh
        String publicId = "smartvn/" + UUID.randomUUID().toString();

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "overwrite", true // Cho phép ghi đè nếu public_id đã tồn tại
        ));

        return Map.of(
                "url", uploadResult.get("url").toString(),
                "public_id", publicId
        );
    }

    /**
     * Xóa file khỏi Cloudinary dựa trên public_id.
     *
     * @param publicId ID công khai của file trên Cloudinary.
     * @throws IOException Nếu có lỗi trong quá trình xóa file.
     */
    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}