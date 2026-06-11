package com.princesses7.findy.recommendation.chatbot.rag.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.chatbot.rag.entity.RagDocument;

public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {

	List<RagDocument> findByActiveTrueOrderByUpdatedAtDesc();

	List<RagDocument> findBySourceNameAndActiveTrue(String sourceName);
}