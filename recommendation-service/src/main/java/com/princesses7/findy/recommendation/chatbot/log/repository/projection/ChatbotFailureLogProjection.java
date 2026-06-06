package com.princesses7.findy.recommendation.chatbot.log.repository.projection;

import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;

public interface ChatbotFailureLogProjection {

	Long getChatbotLogId();

	Long getUserId();

	Long getChatSessionId();

	ChatIntent getIntent();

	String getKeyword();

	String getRequestMessage();

	String getFailureReason();

	LocalDateTime getCreatedAt();
}