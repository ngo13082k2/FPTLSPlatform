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
        validateFileType(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(file);
        String fileNameWithTimestamp = originalFilename != null
                ? originalFilename.replace(fileExtension, "") + "_" + System.currentTimeMillis() + fileExtension
                : "uploaded_file_" + System.currentTimeMillis() + fileExtension;


        Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "raw",
                "public_id", "uploads/" + fileNameWithTimestamp,
                "overwrite", true,
                "type", "upload"
        );


        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);


        return uploadResult.get("secure_url").toString();
    }

    private void validateFileType(MultipartFile file) {

        String fileExtension = getFileExtension(file);
        if (!fileExtension.equalsIgnoreCase(".pdf") && !fileExtension.equalsIgnoreCase(".txt") &&
                !fileExtension.equalsIgnoreCase(".docx")) {
            throw new IllegalArgumentException("Only PDF, TXT, and DOCX files are allowed.");
        }
    }

    private String getFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
    }
}
