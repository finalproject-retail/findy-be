package com.princesses7.findy.recommendation.chatbot.dto;

import java.util.List;

public record ChatbotProductSuitabilityJudgeResponse(
	List<ChatbotProductSuitabilityJudgeItem> items
) {

	public List<ChatbotProductSuitabilityJudgeItem> safeItems() {
		if (items == null) {
			return List.of();
		}

		return items;
	}
}