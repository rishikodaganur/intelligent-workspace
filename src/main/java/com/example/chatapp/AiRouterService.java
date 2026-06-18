package com.example.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class AiRouterService {

    @Autowired
    private GroqService groqService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private WebSearchService webSearchService;

    public String getResponse(String question, String memory) {

        // 1. Get the current time
        ZonedDateTime nowLocal = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        String formattedDate = nowLocal.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a z"));

        // 2. SEARCH THE WEB - Using ONLY the clean user question!
        String realTimeFacts = webSearchService.search(question);

        // Fallback safety if Tavily fails or returns an error string
        if (realTimeFacts == null || realTimeFacts.contains("unavailable") || realTimeFacts.isBlank()) {
            realTimeFacts = "No real-time web results available at the moment.";
        }

        // 3. PRINT TO LOGS: Let's see how beautiful the clean search looks now
        System.out.println("\n--- TAVILY SEARCH RESULT ---");
        System.out.println("Clean Query sent to Tavily: " + question);
        System.out.println("Result: " + realTimeFacts);
        System.out.println("----------------------------\n");

        // 4. Build the Enriched Context Prompt combining System Rules, Memory, and Web
        // Facts
        String enrichedPrompt = "IMPORTANT: You are a helpful AI assistant in a collaborative chat room.\n" +
                "Current Date/Time: " + formattedDate + "\n\n" +
                "Here is the recent conversation history for context:\n" + memory + "\n" +
                "Real-time Web Search Data:\n" + realTimeFacts + "\n\n" +
                "INSTRUCTION: Use the Real-time Web Search Data above to answer the user's question accurately. " +
                "Prioritize these facts over your internal training data cutoff.\n\n" +
                "User Question: " + question;

        // 5. Route to Groq (with Gemini fallback)
        try {
            String groqResponse = groqService.getAnswer(enrichedPrompt);
            if (groqResponse.contains("Sorry") && !groqResponse.contains("Gemini")) {
                throw new Exception("Groq fallback triggered");
            }
            return groqResponse;
        } catch (Exception e) {
            System.out.println("Routing to Gemini fallback due to: " + e.getMessage());
            return geminiService.getAnswer(enrichedPrompt);
        }
    }
}