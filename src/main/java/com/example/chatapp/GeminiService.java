package com.example.chatapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    // We highly recommend using gemini-1.5-flash as it is exceptionally stable on
    // the free tier
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=";

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAnswer(String promptText) {
        String url = API_URL + apiKey;

        // Build the request payload required by the Gemini v1beta API
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("parts", List.of(Map.of("text", promptText)));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contentMap));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // --- RETRY MECHANISM ---
        int maxRetries = 3;
        int retryDelay = 1000; // 1 second

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List candidates = (List) response.getBody().get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map firstCandidate = (Map) candidates.get(0);
                        Map content = (Map) firstCandidate.get("content");
                        List parts = (List) content.get("parts");
                        Map firstPart = (Map) parts.get(0);
                        return (String) firstPart.get("text");
                    }
                }
            } catch (Exception e) {
                System.err.println("Gemini API attempt " + attempt + " failed: " + e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay *= 2; // Exponential backoff (1s -> 2s -> 4s)
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return "Sorry, my AI brain is currently experiencing high demand and could not process the request after multiple attempts. Please try again shortly.";
    }
}