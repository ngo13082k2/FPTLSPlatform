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

    public String uploadImage(MultipartFile file) throws IOException {
        String fileExtension = getFileExtension(file);
        String fileNameWithExtension = "document" + fileExtension;
        Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "raw", // Chỉ định loại tài nguyên là "raw" (chưa qua xử lý)
                "public_id", fileNameWithExtension // Đặt tên tệp PDF
        );
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

        return uploadResult.get("url").toString();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto")); // "auto" cho phép nhiều loại file
        return uploadResult.get("url").toString();
    }

    private String getFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
    }


}
