package com.princesses7.findy.recommendation.chatbot.log.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotFailureLogProjection;

public record AdminChatbotFailureLogResponse(
	Long chatbotLogId,
	Long userId,
	Long chatSessionId,
	ChatIntent intent,
	String keyword,
	String requestMessage,
	String failureReason,
	LocalDateTime createdAt
) {

	public static AdminChatbotFailureLogResponse from(ChatbotFailureLogProjection projection) {
		return new AdminChatbotFailureLogResponse(
			projection.getChatbotLogId(),
			projection.getUserId(),
			projection.getChatSessionId(),
			projection.getIntent(),
			projection.getKeyword(),
			projection.getRequestMessage(),
			projection.getFailureReason(),
			projection.getCreatedAt()
		);
	}
}