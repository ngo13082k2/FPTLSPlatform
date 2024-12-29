package com.example.FPTLSPlatform.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Giới hạn kích thước tệp (ví dụ: 100MB)
        long maxFileSizeInBytes = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxFileSizeInBytes) {
            throw new IllegalArgumentException("Tệp vượt quá kích thước cho phép là 100MB!");
        }

        // Lấy phần mở rộng của file
        String fileExtension = getFileExtension(file);
        if (fileExtension == null || fileExtension.isEmpty()) {
            throw new IllegalArgumentException("File không có phần mở rộng hợp lệ!");
        }

        String fileNameWithExtension = "document_" + System.currentTimeMillis() + fileExtension;

        Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "raw",
                "folder", "uploads",
                "public_id", fileNameWithExtension,
                "type", "upload",
                "chunk_size", 10 * 1024 * 1024 // Chia thành các phần 10MB
        );


        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

        return uploadResult.get("url").toString();
    }



    private String getFileExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.lastIndexOf(".") == -1) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf("."));
    }
}
