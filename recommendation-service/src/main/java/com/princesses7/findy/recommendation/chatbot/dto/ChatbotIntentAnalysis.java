package com.princesses7.findy.recommendation.chatbot.dto;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;

public record ChatbotIntentAnalysis(
	ChatIntent intent,
	String keyword,
	boolean shoppingDataRequired
) {

	public static ChatbotIntentAnalysis general() {
		return new ChatbotIntentAnalysis(
			ChatIntent.GENERAL,
			null,
			false
		);
	}
}