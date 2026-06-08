package com.princesses7.findy.recommendation.chatbot.voice.dto.response;

import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;

public record ChatbotVoiceMessageResponse(
	String transcribedText,
	ChatbotMessageResponse chatbotResponse
) {
}