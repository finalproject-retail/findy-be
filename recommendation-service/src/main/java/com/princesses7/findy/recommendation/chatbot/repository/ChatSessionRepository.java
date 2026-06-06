package com.princesses7.findy.recommendation.chatbot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.chatbot.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

	Optional<ChatSession> findByChatSessionIdAndUserId(Long chatSessionId, Long userId);

	List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);
}