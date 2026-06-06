package com.princesses7.findy.recommendation.chatbot.dto.response;

import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;

public record ChatbotMessageResponse(
	Long sessionId,
	String answer,
	ChatbotShoppingContextResponse shoppingContext,
	RagContextResponse ragContext
) {
}