package com.example.chatapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    // Spring automatically writes the SQL: SELECT * FROM chat_users WHERE username
    // = ?
    Optional<ChatUser> findByUsername(String username);
}