package com.example.chatapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // Spring automatically writes the SQL: SELECT * FROM chat_rooms WHERE name = ?
    Optional<ChatRoom> findByName(String name);
}