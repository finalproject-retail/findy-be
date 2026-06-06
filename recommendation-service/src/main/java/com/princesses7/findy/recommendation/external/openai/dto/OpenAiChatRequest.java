package com.princesses7.findy.recommendation.external.openai.dto;

import java.util.List;

public record OpenAiChatRequest(
	String model,
	List<OpenAiChatMessage> messages,
	Double temperature,
	Integer max_tokens
) {
}