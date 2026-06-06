package com.princesses7.findy.recommendation.chatbot.dto.response;

public record ChatbotMessageResponse(
	Long sessionId,
	String answer,
	ChatbotShoppingContextResponse shoppingContext
) {
}