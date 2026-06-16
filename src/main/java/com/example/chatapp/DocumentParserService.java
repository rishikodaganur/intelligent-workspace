package com.example.chatapp;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentParserService {

    private final EmbeddingAiService embeddingAiService;
    private final DocumentChunkRepository chunkRepository;

    public DocumentParserService(EmbeddingAiService embeddingAiService, DocumentChunkRepository chunkRepository) {
        this.embeddingAiService = embeddingAiService;
        this.chunkRepository = chunkRepository;
    }

    public void processAndStorePdf(MultipartFile file, String roomId) {
        new Thread(() -> {
            try {
                System.out.println("Starting PDF RAG Ingestion for: " + file.getOriginalFilename());

                // UPGRADED: PDFBox 3.0+ uses Loader.loadPDF instead of PDDocument.load
                byte[] fileBytes = file.getBytes();
                try (PDDocument document = Loader.loadPDF(fileBytes)) {
                    PDFTextStripper pdfStripper = new PDFTextStripper();
                    String fullText = pdfStripper.getText(document);

                    String[] paragraphs = fullText.split("\\n\\n");

                    for (String chunk : paragraphs) {
                        if (chunk.trim().length() < 40)
                            continue;

                        String vectorString = embeddingAiService.generateEmbedding(chunk);

                        if (vectorString != null) {
                            chunkRepository.saveChunkWithVector(roomId, file.getOriginalFilename(), chunk.trim(),
                                    vectorString);
                        }
                    }
                    System.out.println("Successfully vectorized and stored PDF: " + file.getOriginalFilename());
                }
            } catch (Exception e) {
                System.err.println("Failed to parse PDF: " + e.getMessage());
            }
        }).start();
    }
}