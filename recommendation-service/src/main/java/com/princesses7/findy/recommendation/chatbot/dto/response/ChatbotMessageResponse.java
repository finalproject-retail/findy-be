package com.princesses7.findy.recommendation.chatbot.dto.response;

import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.support.ChatbotFailureType;
import com.princesses7.findy.recommendation.chatbot.support.ChatbotResponseStatus;

public record ChatbotMessageResponse(
	Long sessionId,
	String answer,
	ChatbotShoppingContextResponse shoppingContext,
	RagContextResponse ragContext,
	ChatbotResponseStatus status,
	ChatbotFailureType failureType
) {

	public ChatbotMessageResponse(
		Long sessionId,
		String answer,
		ChatbotShoppingContextResponse shoppingContext,
		RagContextResponse ragContext
	) {
		this(
			sessionId,
			answer,
			shoppingContext,
			ragContext,
			ChatbotResponseStatus.SUCCESS,
			null
		);
	}

	public static ChatbotMessageResponse success(
		Long sessionId,
		String answer,
		ChatbotShoppingContextResponse shoppingContext,
		RagContextResponse ragContext
	) {
		return new ChatbotMessageResponse(
			sessionId,
			answer,
			shoppingContext,
			ragContext,
			ChatbotResponseStatus.SUCCESS,
			null
		);
	}

	public static ChatbotMessageResponse fallback(
		Long sessionId,
		String fallbackMessage,
		ChatbotFailureType failureType
	) {
		return new ChatbotMessageResponse(
			sessionId,
			fallbackMessage,
			null,
			null,
			ChatbotResponseStatus.FALLBACK,
			failureType
		);
	}
}