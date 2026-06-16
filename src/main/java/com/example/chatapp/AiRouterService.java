package com.example.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiRouterService {

    @Autowired
    private GroqService groqService;

    @Autowired
    private GeminiService geminiService;

    public String getResponse(String prompt) {
        // Strategy: Try Groq first for ultra-low latency
        try {
            String groqResponse = groqService.getAnswer(prompt);
            // If Groq returns an error message, catch it and try Gemini
            if (groqResponse.contains("Sorry") && !groqResponse.contains("Gemini")) {
                throw new Exception("Groq fallback triggered");
            }
            return groqResponse;
        } catch (Exception e) {
            System.out.println("Routing to Gemini fallback due to: " + e.getMessage());
            // Fallback to Gemini if Groq fails
            return geminiService.getAnswer(prompt);
        }
    }
}