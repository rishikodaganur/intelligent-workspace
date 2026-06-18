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

    // THE UPGRADE: Inject our new search service
    @Autowired
    private WebSearchService webSearchService;

    public String getResponse(String prompt) {

        // 1. Get the current time
        ZonedDateTime nowLocal = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        String formattedDate = nowLocal.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a z"));

        // 2. SEARCH THE WEB
        // We pass the user's prompt directly to Tavily
        String realTimeFacts = webSearchService.search(prompt);

        // 3. Build the Ultimate Context Block
        String systemContext = "[System Context: The current date and time is " + formattedDate + ". " +
                "Here is real-time web data regarding the user's query: " + realTimeFacts + " " +
                "Use these facts to answer the user's prompt accurately.]\n\nUser Request: ";

        String enrichedPrompt = systemContext + prompt;

        // 4. Route to Groq (with Gemini fallback)
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