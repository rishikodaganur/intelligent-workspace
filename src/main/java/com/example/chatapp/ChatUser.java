package com.example.chatapp;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_users")
public class ChatUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usernames must be unique so people can't impersonate each other
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Constructors
    public ChatUser() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}