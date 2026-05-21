package com.princesses7.findy.recommendation.external.openai.dto.response;

import java.util.List;

public record OpenAiEmbeddingResponse(
	List<OpenAiEmbeddingData> data
) {

	public List<Double> firstEmbedding() {
		if (data == null || data.isEmpty()) {
			return List.of();
		}

		return data.get(0).embedding();
	}
}