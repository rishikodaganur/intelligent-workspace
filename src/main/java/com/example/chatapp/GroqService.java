package com.example.chatapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${GROQ_API_KEY}")
    private String apiKey;

    private final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    public String getAnswer(String promptText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Groq uses OpenAI's message format payload structure
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", promptText);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile"); // Extremely capable, fast model
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_URL, entity, Map.class);

            if (response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map messageObj = (Map) firstChoice.get("message");
                    return (String) messageObj.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("Groq API Error: " + e.getMessage());
        }

        return "Sorry, the Groq inference engine could not be reached.";
    }
}