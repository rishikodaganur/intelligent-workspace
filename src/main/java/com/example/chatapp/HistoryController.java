package com.example.chatapp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/chat/room")
public class HistoryController {

    private final ChatMessageRepository chatMessageRepository;

    public HistoryController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    // Matches the JS fetch: /chat/room/{roomName}/history?page=0&size=15
    @GetMapping("/{roomName}/history")
    public ResponseEntity<List<ChatMessage>> getRoomHistory(
            @PathVariable String roomName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        // 1. Create a page request
        Pageable pageable = PageRequest.of(page, size);

        // 2. Fetch the chunk of messages from the database
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomNameOrderByIdDesc(roomName, pageable);

        // 3. Extract the actual list of messages from the Page object
        List<ChatMessage> messages = messagePage.getContent();

        // 4. Because we fetched them Descending (newest first), we need to reverse the
        // list
        // before sending it to the frontend so they render in the correct top-to-bottom
        // order!
        // (Create a mutable copy first to avoid unsupported operation exceptions)
        java.util.ArrayList<ChatMessage> modifiableList = new java.util.ArrayList<>(messages);
        // Collections.reverse(modifiableList);

        return ResponseEntity.ok(modifiableList);
    }
}