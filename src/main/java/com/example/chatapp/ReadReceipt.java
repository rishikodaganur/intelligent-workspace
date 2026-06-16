package com.example.chatapp;

public class ReadReceipt {
    private String messageId;
    private String readerUsername;
    private String roomName;

    // Default constructor for Spring JSON parsing
    public ReadReceipt() {
    }

    public ReadReceipt(String messageId, String readerUsername, String roomName) {
        this.messageId = messageId;
        this.readerUsername = readerUsername;
        this.roomName = roomName;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getReaderUsername() {
        return readerUsername;
    }

    public void setReaderUsername(String readerUsername) {
        this.readerUsername = readerUsername;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}