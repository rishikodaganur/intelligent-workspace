package com.example.chatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmbeddingAiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateEmbedding(String text) {
        try {
            // UPGRADED: Using the active 2026 gemini-embedding-001 model
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key="
                    + apiKey;

            // Sanitize the text so quotes and newlines don't break the JSON payload
            String safeText = text.replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");

            // UPGRADED: Explicitly truncate the massive 3072D vector down to our exact 768D
            // database requirement
            String requestJson = "{" +
                    "\"model\": \"models/gemini-embedding-001\", " +
                    "\"content\": {\"parts\":[{\"text\": \"" + safeText + "\"}]}, " +
                    "\"outputDimensionality\": 768" +
                    "}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

            String response = restTemplate.postForObject(url, request, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            // Extract the vector array and convert it to a string format PostgreSQL
            // accepts: "[0.1, 0.2, ...]"
            JsonNode valuesNode = rootNode.path("embedding").path("values");
            return valuesNode.toString();

        } catch (Exception e) {
            System.err.println("Embedding Error: " + e.getMessage());
            return null;
        }
    }
}