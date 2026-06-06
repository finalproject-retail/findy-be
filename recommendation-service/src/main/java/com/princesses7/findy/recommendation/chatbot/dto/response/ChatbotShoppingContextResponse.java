package com.princesses7.findy.recommendation.chatbot.dto.response;

import java.util.List;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;

public record ChatbotShoppingContextResponse(
	Long storeId,
	String keyword,
	ChatIntent intent,
	List<ChatbotProductContextResponse> products
) {

	public boolean hasData() {
		return products != null && !products.isEmpty();
	}

	public static ChatbotShoppingContextResponse empty(
		Long storeId,
		String keyword,
		ChatIntent intent
	) {
		return new ChatbotShoppingContextResponse(
			storeId,
			keyword,
			intent,
			List.of()
		);
	}
}