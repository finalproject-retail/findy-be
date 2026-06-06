package com.princesses7.findy.recommendation.chatbot.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.entity.ChatMessage;
import com.princesses7.findy.recommendation.chatbot.entity.ChatSenderType;

public record ChatMessageItemResponse(
	Long chatMessageId,
	ChatSenderType senderType,
	String content,
	ChatIntent intent,
	LocalDateTime createdAt
) {

	public static ChatMessageItemResponse from(ChatMessage chatMessage) {
		return new ChatMessageItemResponse(
			chatMessage.getChatMessageId(),
			chatMessage.getSenderType(),
			chatMessage.getContent(),
			chatMessage.getIntent(),
			chatMessage.getCreatedAt()
		);
	}
}