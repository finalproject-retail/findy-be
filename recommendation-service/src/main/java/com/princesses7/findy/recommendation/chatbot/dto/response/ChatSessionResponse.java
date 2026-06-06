package com.princesses7.findy.recommendation.chatbot.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.chatbot.entity.ChatSession;

public record ChatSessionResponse(
	Long chatSessionId,
	String title,
	String lastMessage,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static ChatSessionResponse from(ChatSession chatSession) {
		return new ChatSessionResponse(
			chatSession.getChatSessionId(),
			chatSession.getTitle(),
			chatSession.getLastMessage(),
			chatSession.getCreatedAt(),
			chatSession.getUpdatedAt()
		);
	}
}