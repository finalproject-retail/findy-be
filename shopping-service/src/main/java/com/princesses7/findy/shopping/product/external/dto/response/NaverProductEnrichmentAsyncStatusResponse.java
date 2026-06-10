package com.princesses7.findy.shopping.product.external.dto.response;

import java.time.LocalDateTime;

public record NaverProductEnrichmentAsyncStatusResponse(
	boolean running,
	String status,
	int offset,
	int limit,
	Integer nextOffset,
	boolean hasNext,
	int batchCount,
	int processedProductCount,
	int matchedCount,
	int reviewRequiredCount,
	int skippedCount,
	int appliedCount,
	String errorMessage,
	LocalDateTime startedAt,
	LocalDateTime updatedAt,
	LocalDateTime finishedAt
) {
}
