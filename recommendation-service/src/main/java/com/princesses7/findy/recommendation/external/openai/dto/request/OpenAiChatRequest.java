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

	Double temperature
) {

	public static OpenAiChatRequest plain(
		String model,
		List<OpenAiChatMessage> messages
	) {
		return new OpenAiChatRequest(
			model,
			messages,
			null,
			0.2
		);
	}

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