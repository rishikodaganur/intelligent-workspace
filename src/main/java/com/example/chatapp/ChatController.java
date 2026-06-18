package com.example.chatapp;

import org.springframework.data.domain.PageRequest;
import java.util.Collections;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageRepository messageRepository;
    private final RagChatService ragChatService;
    private final ChatRoomRepository chatRoomRepository;
    private final DocumentChunkRepository chunkRepository;
    private final GroqService groqService;
    private final AiRouterService aiRouterService;

    public ChatController(SimpMessageSendingOperations messagingTemplate,
            ChatMessageRepository messageRepository,
            RagChatService ragChatService,
            ChatRoomRepository chatRoomRepository,
            DocumentChunkRepository chunkRepository,
            GroqService groqService,
            AiRouterService aiRouterService) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.ragChatService = ragChatService;
        this.chatRoomRepository = chatRoomRepository;
        this.chunkRepository = chunkRepository;
        this.groqService = groqService;
        this.aiRouterService = aiRouterService;
    }

    @MessageMapping("/chat/{roomName}/sendMessage")
    public void sendMessage(@DestinationVariable String roomName, @Payload ChatMessage chatMessage) {

        // 1. Fetch and attach the Room object
        ChatRoom room = chatRoomRepository.findByName(roomName).orElse(null);
        if (room != null) {
            chatMessage.setRoom(room);
        }

        // 2. Save and broadcast the human message
        messageRepository.save(chatMessage);
        messagingTemplate.convertAndSend("/topic/" + roomName, chatMessage);

        // --- THE INTELLIGENT BOT INTERCEPTOR ---
        if (chatMessage.getContent() != null && chatMessage.getContent().toLowerCase().startsWith("@bot")) {

            String question = chatMessage.getContent().substring(4).trim();
            String selectedModel = chatMessage.getModel() != null ? chatMessage.getModel().toLowerCase() : "groq";
            String qLower = question.toLowerCase();

            // 1. Keyword Detection
            boolean isDocumentQuery = qLower.contains("pdf") || qLower.contains("file")
                    || qLower.contains("document") || qLower.contains("certificate")
                    || qLower.contains("score") || qLower.contains("mark")
                    || qLower.contains("grade") || qLower.contains("nptel");

            boolean hasDocuments = chunkRepository.countByRoomId(roomName) > 0;
            String aiResponseText;

            // 2. Fetch the last 5 messages for Conversational Memory
            java.util.List<ChatMessage> recentMessages = messageRepository.findByRoomNameOrderByIdDesc(
                    roomName, PageRequest.of(0, 5)).getContent();

            // Reverse them so they are in chronological order (oldest to newest)
            java.util.List<ChatMessage> chronologicalHistory = new java.util.ArrayList<>(recentMessages);
            java.util.Collections.reverse(chronologicalHistory);

            // Build the memory string
            StringBuilder memory = new StringBuilder();
            for (ChatMessage msg : chronologicalHistory) {
                if (msg.getContent() != null && !msg.getContent().startsWith("[Attachment")) {
                    String senderName = msg.getSender() != null ? msg.getSender() : "User";
                    // Clean up the @bot from previous user messages so it doesn't confuse the AI
                    String cleanContent = msg.getContent().replace("@bot", "").trim();
                    memory.append(senderName).append(": ").append(cleanContent).append("\n");
                }
            }

            // 3. Route the Request
            if (isDocumentQuery && hasDocuments) {
                // For RAG, we stick to the specific document question
                aiResponseText = ragChatService.askQuestion(roomName, question);
            } else {
                // For General Chat, we inject the memory context!
                String contextualPrompt = "You are a helpful AI assistant in a collaborative chat room. " +
                        "Here is the recent conversation history for context:\n\n" +
                        memory.toString() + "\n\n" +
                        "Now, please respond to the latest message from " + chatMessage.getSender() + ": " + question;

                // FIX: Always route through aiRouterService so both Groq and Gemini get
                // real-time search!
                aiResponseText = aiRouterService.getResponse(contextualPrompt);
            }

            // 4. Create and Broadcast Bot Response
            ChatMessage botMessage = new ChatMessage();
            botMessage.setSender(selectedModel.toUpperCase() + " Assistant");
            botMessage.setModel(selectedModel);
            botMessage.setContent(aiResponseText);

            if (room != null) {
                botMessage.setRoom(room);
            }

            messageRepository.save(botMessage);
            messagingTemplate.convertAndSend("/topic/" + roomName, botMessage);
        }
    }
}