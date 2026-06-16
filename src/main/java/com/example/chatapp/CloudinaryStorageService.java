package com.example.chatapp;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Generate a unique identifier
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");

        // Upload to Cloudinary with Auto-Optimization parameters
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", uniqueFileName,
                "resource_type", "auto" // Automatically handles images, videos, and raw files (PDFs)
        ));

        // Retrieve the optimized, secure HTTPS URL provided by Cloudinary
        return uploadResult.get("secure_url").toString();
    }
}