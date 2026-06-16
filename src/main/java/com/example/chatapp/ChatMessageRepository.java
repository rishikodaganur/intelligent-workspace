package com.example.chatapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Fetch only the messages that belong to a specific room
    List<ChatMessage> findByRoomId(Long roomId);

    Page<ChatMessage> findByRoomId(Long roomId, Pageable pageable);

    List<ChatMessage> findByRoomName(String roomName);

    Page<ChatMessage> findByRoomNameOrderByIdDesc(String roomName, Pageable pageable);

}