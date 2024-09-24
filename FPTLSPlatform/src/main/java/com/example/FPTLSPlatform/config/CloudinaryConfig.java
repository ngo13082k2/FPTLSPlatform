package com.example.FPTLSPlatform.config;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dplcrmozt",
                "api_key", "748346212269242",
                "api_secret", "0L6oNsEzqVUx5HWDsguYCPouFEI"));
    }
}
