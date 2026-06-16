package com.example.chatapp;

import jakarta.persistence.*;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String chunkText;

    // Constructors, Getters, Setters
    public DocumentChunk() {
    }
}