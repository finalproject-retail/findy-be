package com.princesses7.findy.recommendation.chatbot.log.dto.response;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotIntentCountProjection;

public record AdminChatbotIntentCountResponse(
	ChatIntent intent,
	Long count
) {

	public static AdminChatbotIntentCountResponse from(ChatbotIntentCountProjection projection) {
		return new AdminChatbotIntentCountResponse(
			projection.getIntent(),
			projection.getCount()
		);
	}
}