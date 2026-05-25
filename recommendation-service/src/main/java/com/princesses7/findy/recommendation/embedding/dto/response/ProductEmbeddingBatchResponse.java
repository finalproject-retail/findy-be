package com.princesses7.findy.recommendation.embedding.dto.response;

public record ProductEmbeddingBatchResponse(
	int requestedCount,
	int savedCount
) {
}