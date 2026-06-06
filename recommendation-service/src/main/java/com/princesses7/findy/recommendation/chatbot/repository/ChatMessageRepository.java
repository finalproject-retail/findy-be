package com.princesses7.findy.recommendation.chatbot.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.chatbot.entity.ChatMessage;
import com.princesses7.findy.recommendation.chatbot.entity.ChatSession;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession chatSession);

	@Query("""
		SELECT m
		FROM ChatMessage m
		WHERE m.chatSession = :chatSession
		ORDER BY m.createdAt DESC
		""")
	List<ChatMessage> findRecentMessages(
		@Param("chatSession") ChatSession chatSession,
		Pageable pageable
	);
}