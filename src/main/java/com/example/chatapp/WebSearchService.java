package com.example.chatapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class WebSearchService {

    private final RestClient restClient;

    @Value("${TAVILY_API_KEY}")
    private String apiKey;

    public WebSearchService() {
        // Modern Spring Boot HTTP client
        this.restClient = RestClient.create();
    }

    public String search(String query) {
        try {
            // Build the JSON request payload
            Map<String, Object> requestBody = Map.of(
                    "api_key", apiKey,
                    "query", query,
                    "search_depth", "basic",
                    "include_answer", true // This forces Tavily to return a clean summary!
            );

            // Make the POST request to the API
            Map response = restClient.post()
                    .uri("https://api.tavily.com/search")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // Extract the clean text answer
            if (response != null && response.get("answer") != null) {
                return (String) response.get("answer");
            }
            return "No recent data found on this topic.";

        } catch (Exception e) {
            System.out.println("Search failed: " + e.getMessage());
            return "Real-time search is currently unavailable.";
        }
    }
}