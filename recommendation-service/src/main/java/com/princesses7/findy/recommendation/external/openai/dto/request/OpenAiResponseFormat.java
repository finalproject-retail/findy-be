package com.princesses7.findy.recommendation.external.openai.dto.request;

public record OpenAiResponseFormat(
	String type
) {

	public static OpenAiResponseFormat jsonObject() {
		return new OpenAiResponseFormat("json_object");
	}
}