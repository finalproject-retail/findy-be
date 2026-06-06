package com.princesses7.findy.recommendation.external.openai.dto;

public record OpenAiChatMessage(
	String role,
	String content
) {
}