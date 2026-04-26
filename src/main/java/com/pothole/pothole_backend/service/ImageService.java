package com.pothole.pothole_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                log.warn("No image provided, using placeholder");
                return "https://res.cloudinary.com/demo/image/upload/pothole_placeholder.jpg";
            }

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "pothole-mp",
                            "resource_type", "image"
                    )
            );

            String url = (String) uploadResult.get("secure_url");
            log.info("Image uploaded to Cloudinary: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            // Return placeholder instead of failing
            return "https://res.cloudinary.com/demo/image/upload/pothole_placeholder.jpg";
        }
    }
}