package com.princesses7.findy.recommendation.external.openai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenAiChatRequest(
	String model,
	List<OpenAiChatMessage> messages,

	@JsonProperty("response_format")
	OpenAiResponseFormat responseFormat,

	Double temperature
) {

	public static OpenAiChatRequest json(
		String model,
		List<OpenAiChatMessage> messages
	) {
		return new OpenAiChatRequest(
			model,
			messages,
			OpenAiResponseFormat.jsonObject(),
			0.0
		);
	}
}