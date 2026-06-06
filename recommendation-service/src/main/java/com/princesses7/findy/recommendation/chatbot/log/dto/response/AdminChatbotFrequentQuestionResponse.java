package com.princesses7.findy.recommendation.chatbot.log.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotFrequentQuestionProjection;

public record AdminChatbotFrequentQuestionResponse(
	String question,
	Long count,
	LocalDateTime lastAskedAt
) {

	public static AdminChatbotFrequentQuestionResponse from(ChatbotFrequentQuestionProjection projection) {
		return new AdminChatbotFrequentQuestionResponse(
			projection.getQuestion(),
			projection.getCount(),
			projection.getLastAskedAt()
		);
	}
}