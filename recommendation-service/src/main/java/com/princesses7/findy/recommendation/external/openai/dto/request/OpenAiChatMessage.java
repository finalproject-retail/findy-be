package com.princesses7.findy.recommendation.external.openai.dto.request;

public record OpenAiChatMessage(
	String role,
	String content
) {

	public static OpenAiChatMessage system(String content) {
		return new OpenAiChatMessage("system", content);
	}

	public static OpenAiChatMessage user(String content) {
		return new OpenAiChatMessage("user", content);
	}
}