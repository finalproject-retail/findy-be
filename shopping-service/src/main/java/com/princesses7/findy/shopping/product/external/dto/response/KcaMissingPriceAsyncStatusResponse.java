package com.princesses7.findy.shopping.product.external.dto.response;

import java.time.LocalDateTime;

public record KcaMissingPriceAsyncStatusResponse(
	boolean running,
	String status,
	String goodInspectDay,
	int offset,
	int limit,
	Integer nextOffset,
	boolean hasNext,
	int batchCount,
	int productInfoCount,
	int processedProductCount,
	int sourceItemCount,
	int aggregatedItemCount,
	int matchedCount,
	int reviewRequiredCount,
	int skippedCount,
	int appliedCount,
	String kcaResultCode,
	String kcaResultMessage,
	String errorMessage,
	LocalDateTime startedAt,
	LocalDateTime updatedAt,
	LocalDateTime finishedAt
) {
}
