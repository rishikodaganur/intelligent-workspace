package com.example.chatapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO document_chunks (room_id, file_name, chunk_text, embedding) VALUES (:roomId, :fileName, :chunkText, CAST(:embedding AS vector))", nativeQuery = true)
    void saveChunkWithVector(
            @Param("roomId") String roomId,
            @Param("fileName") String fileName,
            @Param("chunkText") String chunkText,
            @Param("embedding") String embedding);

    // --- NEW: RAG Vector Search Query ---
    // Uses pgvector's cosine distance operator (<=>) to find the top 3 closest
    // matching paragraphs
    @Query(value = "SELECT chunk_text FROM document_chunks WHERE room_id = :roomId ORDER BY embedding <=> CAST(:queryEmbedding AS vector) LIMIT 3", nativeQuery = true)
    List<String> findSimilarChunks(@Param("roomId") String roomId, @Param("queryEmbedding") String queryEmbedding);

    long countByRoomId(String roomId);

}