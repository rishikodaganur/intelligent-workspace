package com.example.chatapp;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class ReadReceiptController {

    private final SimpMessageSendingOperations messagingTemplate;

    public ReadReceiptController(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // This catches the signal from the frontend when a message scrolls into view
    @MessageMapping("/chat/{roomName}/read")
    public void handleReadReceipt(@DestinationVariable String roomName, @Payload ReadReceipt receipt) {

        // Security check: ensure the payload matches the room
        receipt.setRoomName(roomName);

        // Broadcast the receipt to everyone in the room
        // The sender's UI will hear this and turn their message tick "Blue"
        messagingTemplate.convertAndSend("/topic/" + roomName + "/receipts", receipt);
    }
}