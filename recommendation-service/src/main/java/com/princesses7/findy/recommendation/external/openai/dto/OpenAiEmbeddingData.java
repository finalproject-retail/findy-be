package com.princesses7.findy.recommendation.external.openai.dto;

import java.util.List;

public record OpenAiEmbeddingData(
	List<Double> embedding
) {
}