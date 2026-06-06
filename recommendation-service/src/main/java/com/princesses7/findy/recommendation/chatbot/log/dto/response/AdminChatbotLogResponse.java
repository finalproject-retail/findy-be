package com.princesses7.findy.recommendation.chatbot.log.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLog;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLogStatus;

public record AdminChatbotLogResponse(
	Long chatbotLogId,
	Long userId,
	Long chatSessionId,
	ChatIntent intent,
	String keyword,
	String requestMessage,
	String responseMessage,
	ChatbotLogStatus status,
	String failureReason,
	Long durationMs,
	LocalDateTime createdAt
) {

	public static AdminChatbotLogResponse from(ChatbotLog chatbotLog) {
		return new AdminChatbotLogResponse(
			chatbotLog.getChatbotLogId(),
			chatbotLog.getUserId(),
			chatbotLog.getChatSessionId(),
			chatbotLog.getIntent(),
			chatbotLog.getKeyword(),
			chatbotLog.getRequestMessage(),
			chatbotLog.getResponseMessage(),
			chatbotLog.getStatus(),
			chatbotLog.getFailureReason(),
			chatbotLog.getDurationMs(),
			chatbotLog.getCreatedAt()
		);
	}
}