package com.example.chatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RagChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final EmbeddingAiService embeddingAiService;
    private final DocumentChunkRepository chunkRepository;
    private final AiRouterService aiRouterService; // Inject general AI router fallback
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RagChatService(EmbeddingAiService embeddingAiService,
            DocumentChunkRepository chunkRepository,
            AiRouterService aiRouterService) {
        this.embeddingAiService = embeddingAiService;
        this.chunkRepository = chunkRepository;
        this.aiRouterService = aiRouterService;
    }

    // FIX: Added 'memory' parameter to signature
    public String askQuestion(String roomId, String question, String memory) {
        try {
            // 1. Convert the user's question into a mathematical vector
            String questionVector = embeddingAiService.generateEmbedding(question);
            if (questionVector == null)
                return "Sorry, my embedding engine is offline.";

            // 2. Perform Cosine Similarity Search in the Vector Database
            List<String> relevantChunks = chunkRepository.findSimilarChunks(roomId, questionVector);

            // UPGRADED: If no relevant document chunks are found, fallback successfully by
            // passing BOTH args
            if (relevantChunks == null || relevantChunks.isEmpty()) {
                return aiRouterService.getResponse(question, memory);
            }

            // 3. Combine the retrieved paragraphs into a single context block
            String context = String.join(" ", relevantChunks);

            // 4. Send the Context + Question to the Gemini Chat Model
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key="
                    + apiKey;

            // FIX: Injected conversational memory into the RAG Prompt
            String prompt = "You are an intelligent workspace assistant in a chat room. You have access to the following Document Context.\n"
                    +
                    "1. First, try to answer the user's question using ONLY the Document Context.\n" +
                    "2. If the Document Context does not contain the answer, or if the user is just making casual conversation, IGNORE the context and answer naturally.\n"
                    +
                    "3. Be helpful, concise, and friendly.\n\n" +
                    "Recent Conversation History:\n" + memory + "\n\n" +
                    "Document Context:\n" + context + "\n\n" +
                    "User Message: " + question;

            // --- Let Java build the JSON safely to prevent crashing ---
            java.util.Map<String, Object> textPart = new java.util.HashMap<>();
            textPart.put("text", prompt);

            java.util.Map<String, Object> partsMap = new java.util.HashMap<>();
            partsMap.put("parts", java.util.List.of(textPart));

            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("contents", java.util.List.of(partsMap));

            String requestJson = objectMapper.writeValueAsString(requestBody);
            // ----------------------------------------------------------------------

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

            String response = restTemplate.postForObject(url, request, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            // --- UPGRADED: Null-safe candidate and part extraction ---
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            return "The Document AI analyzed the file but did not return an answer. It may be blocked by safety filters.";

        } catch (Exception e) {
            System.err.println("RAG Chat Error Details:");
            e.printStackTrace(); // Prints the precise error stack trace in your terminal
            return "I encountered an error trying to read the documents: " + e.getMessage();
        }
    }
}