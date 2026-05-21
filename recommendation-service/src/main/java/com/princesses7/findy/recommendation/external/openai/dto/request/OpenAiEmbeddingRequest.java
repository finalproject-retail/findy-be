package com.princesses7.findy.recommendation.external.openai.dto.request;

import java.util.List;

public record OpenAiEmbeddingRequest(
	String model,
	List<String> input,
	Integer dimensions
) {

	public static OpenAiEmbeddingRequest of(
		String model,
		String text,
		Integer dimensions
	) {
		return new OpenAiEmbeddingRequest(
			model,
			List.of(text),
			dimensions
		);
	}
}