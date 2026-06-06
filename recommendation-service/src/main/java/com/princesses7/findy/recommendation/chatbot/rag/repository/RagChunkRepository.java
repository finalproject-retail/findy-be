package com.princesses7.findy.recommendation.chatbot.rag.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.chatbot.rag.entity.RagChunk;
import com.princesses7.findy.recommendation.chatbot.rag.entity.RagDocument;

public interface RagChunkRepository extends JpaRepository<RagChunk, Long> {

	List<RagChunk> findByRagDocumentAndActiveTrueOrderByChunkIndexAsc(RagDocument ragDocument);

	void deleteByRagDocument(RagDocument ragDocument);

	List<RagChunk> findTop30ByActiveTrueOrderByUpdatedAtDesc();
}