package com.example.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    // Endpoint 1: Try to join an existing room
    @PostMapping("/join")
    public ResponseEntity<String> joinRoom(@RequestParam String roomName,
            @RequestParam(required = false) String password) {
        Optional<ChatRoom> roomOpt = chatRoomRepository.findByName(roomName);

        if (roomOpt.isPresent()) {
            ChatRoom room = roomOpt.get();

            // Verify password if the room has one
            if (room.getRoomPassword() != null && !room.getRoomPassword().isEmpty()) {
                if (!room.getRoomPassword().equals(password)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Incorrect password.");
                }
            }
            return ResponseEntity.ok("Access Granted");
        } else {
            // Tell the frontend the room doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room does not exist.");
        }
    }

    // Endpoint 2: Create a brand new room
    @PostMapping("/create")
    public ResponseEntity<String> createRoom(@RequestParam String roomName,
            @RequestParam(required = false) String password) {
        if (chatRoomRepository.findByName(roomName).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Room already exists.");
        }

        ChatRoom newRoom = new ChatRoom();
        newRoom.setName(roomName);
        newRoom.setRoomPassword(password);
        chatRoomRepository.save(newRoom);

        return ResponseEntity.ok("Room created successfully.");
    }

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private GeminiService geminiService;

    // Endpoint 3: AI Summarization
    @GetMapping("/{roomName}/summarize")
    public ResponseEntity<String> summarizeRoom(@PathVariable String roomName) {
        Optional<ChatRoom> roomOpt = chatRoomRepository.findByName(roomName);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found.");
        }

        // Fetch all messages for this room
        // NEW RELATIONAL WAY (Using the room's name)
        List<ChatMessage> history = chatMessageRepository.findByRoomName(roomName);
        if (history.isEmpty()) {
            return ResponseEntity.ok("The room is currently empty. No summary available.");
        }

        // Format the history into a script for the AI to read (e.g., "Rizzi: Hello \n
        // Alice: Hi")
        String chatLog = history.stream()
                .map(msg -> msg.getSender() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));

        // Ask Gemini to summarize it
        String prompt = "You are an AI assistant. Read the following chat log and provide a very brief, 3-bullet-point summary of what was discussed. Do not include pleasantries, just the summary.\n\n"
                + chatLog;
        String summary = geminiService.getAnswer(prompt);

        return ResponseEntity.ok(summary);
    }
}
