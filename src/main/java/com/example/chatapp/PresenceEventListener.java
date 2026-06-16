package com.example.chatapp;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class PresenceEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    // Maps Room Name -> Set of Usernames in that room
    private final ConcurrentHashMap<String, Set<String>> roomUsers = new ConcurrentHashMap<>();
    // Maps Session ID -> Room Name (Handles if they close the tab abruptly)
    private final ConcurrentHashMap<String, String> sessionToRoom = new ConcurrentHashMap<>();
    // Maps Session ID -> Username
    private final ConcurrentHashMap<String, String> sessionToUser = new ConcurrentHashMap<>();

    public PresenceEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 1. Triggered when the JavaScript says "I entered the room"
    @MessageMapping("/chat/{roomName}/join")
    public void joinRoom(@DestinationVariable String roomName, SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getUser() == null)
            return;
        String username = headerAccessor.getUser().getName();
        String sessionId = headerAccessor.getSessionId();

        roomUsers.computeIfAbsent(roomName, k -> ConcurrentHashMap.newKeySet()).add(username);
        sessionToRoom.put(sessionId, roomName);
        sessionToUser.put(sessionId, username);

        // Broadcast ONLY to this specific room's presence channel
        messagingTemplate.convertAndSend("/topic/" + roomName + "/presence", roomUsers.get(roomName));
    }

    // 2. Triggered when the JavaScript says "I clicked Exit Room"
    @MessageMapping("/chat/{roomName}/leave")
    public void leaveRoom(@DestinationVariable String roomName, SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getUser() == null)
            return;
        removeUserFromRoom(roomName, headerAccessor.getUser().getName(), headerAccessor.getSessionId());
    }

    // 3. Triggered if they violently close the browser tab or lose internet
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String roomName = sessionToRoom.get(sessionId);
        String username = sessionToUser.get(sessionId);

        if (roomName != null && username != null) {
            removeUserFromRoom(roomName, username, sessionId);
        }
    }

    private void removeUserFromRoom(String roomName, String username, String sessionId) {
        Set<String> users = roomUsers.get(roomName);
        if (users != null) {
            users.remove(username);
            messagingTemplate.convertAndSend("/topic/" + roomName + "/presence", users);
            if (users.isEmpty())
                roomUsers.remove(roomName); // Clean up empty rooms
        }
        sessionToRoom.remove(sessionId);
        sessionToUser.remove(sessionId);
    }

    // 4. REST endpoint so the frontend can check who is already in the room
    @GetMapping("/api/presence/{roomName}")
    public Set<String> getActiveUsersInRoom(@PathVariable String roomName) {
        return roomUsers.getOrDefault(roomName, Collections.emptySet());
    }
}