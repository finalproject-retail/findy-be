package com.princesses7.findy.recommendation.external.openai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiChatRequest(
	String model,
	List<OpenAiChatMessage> messages,

	@JsonProperty("response_format")
	OpenAiResponseFormat responseFormat,

	Double temperature,

	@JsonProperty("max_tokens")
	Integer maxTokens
) {

	public static OpenAiChatRequest plain(
		String model,
		List<OpenAiChatMessage> messages
	) {
		return plain(model, messages, null);
	}

	public static OpenAiChatRequest plain(
		String model,
		List<OpenAiChatMessage> messages,
		Integer maxTokens
	) {
		return new OpenAiChatRequest(
			model,
			messages,
			null,
			0.2,
			maxTokens
		);
	}

	public static OpenAiChatRequest json(
		String model,
		List<OpenAiChatMessage> messages
	) {
		return json(model, messages, null);
	}

	public static OpenAiChatRequest json(
		String model,
		List<OpenAiChatMessage> messages,
		Integer maxTokens
	) {
		return new OpenAiChatRequest(
			model,
			messages,
			OpenAiResponseFormat.jsonObject(),
			0.0,
			maxTokens
		);
	}
}