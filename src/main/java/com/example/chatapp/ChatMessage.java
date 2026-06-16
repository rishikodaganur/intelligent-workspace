package com.example.chatapp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "chat_messages") // Explicitly naming the table is a good practice for relational databases
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;

    // Expands the database limit to 10,000+ characters
    @Column(columnDefinition = "TEXT")
    private String content;

    // --- THE NEW ARCHITECTURE ---
    // Many Messages belong to One Room
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnore
    private ChatRoom room;
    private String model;

    @Column(columnDefinition = "TEXT")
    private String fileData;

    private String fileName;
    private String fileType;

    public ChatMessage() {
    }

    // Updated constructor to include the room
    public ChatMessage(String sender, String content, ChatRoom room) {
        this.sender = sender;
        this.content = content;
        this.room = room;
    }

    // --- GETTERS & SETTERS ---
    @com.fasterxml.jackson.annotation.JsonProperty("roomId")
    public String getRoomId() {
        return this.room != null ? this.room.getName() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // New getters and setters for the room
    public ChatRoom getRoom() {
        return room;
    }

    public void setRoom(ChatRoom room) {
        this.room = room;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
