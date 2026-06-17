package com.example.chatapp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
// Notice I removed the @RequestMapping from the class level
public class FileUploadController {

    private final CloudinaryStorageService cloudinaryService;
    private final VisionAiService visionAiService;
    private final DocumentParserService documentParserService;

    public FileUploadController(CloudinaryStorageService cloudinaryService,
            VisionAiService visionAiService,
            DocumentParserService documentParserService) {
        this.cloudinaryService = cloudinaryService;
        this.visionAiService = visionAiService;
        this.documentParserService = documentParserService;
    }

    // THE FIX: Highly specific endpoint path that won't collide with anything
    @PostMapping("/api/v1/upload")
    public ResponseEntity<Map<String, String>> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "roomId", required = false) String roomId) {
        try {
            String fileUrl = cloudinaryService.uploadFile(file);
            String aiTags = "";

            if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
                aiTags = visionAiService.generateImageTags(file.getBytes(), file.getContentType());
            } else if (file.getContentType() != null && file.getContentType().equals("application/pdf")
                    && roomId != null) {
                documentParserService.processAndStorePdf(file, roomId);
                aiTags = "[PDF Ingested into AI Memory]";
            }

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("tags", aiTags);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}