package com.princesses7.findy.recommendation.embedding.dto.response;

public record ProductEmbeddingResponse(
	Long productId,
	String model,
	int dimensions
) {
}