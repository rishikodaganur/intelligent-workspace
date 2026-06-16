package com.example.chatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class VisionAiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateImageTags(byte[] imageBytes, String mimeType) {
        try {
            // 1. Convert the raw image into a Base64 string for the AI
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key="
                    + apiKey;
            // 2. Build the exact JSON payload the Gemini Vision API expects
            String requestJson = "{" +
                    "\"contents\": [ {" +
                    "\"parts\": [" +
                    "{\"text\": \"Analyze this image. Return exactly 3 highly relevant, concise keywords describing it. Format them exactly like this: [Keyword1] [Keyword2] [Keyword3]. Do not say anything else.\"},"
                    +
                    "{\"inline_data\": {\"mime_type\": \"" + mimeType + "\", \"data\": \"" + base64Image + "\"}}" +
                    "] } ] }";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

            // 3. Fire the request to Google
            String response = restTemplate.postForObject(url, request, String.class);

            // 4. Parse the AI's response and extract just the text
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText().trim();

        } catch (Exception e) {
            System.err.println("AI Vision Error: " + e.getMessage());
            return ""; // If the AI fails, return an empty string so the upload doesn't crash
        }
    }
}