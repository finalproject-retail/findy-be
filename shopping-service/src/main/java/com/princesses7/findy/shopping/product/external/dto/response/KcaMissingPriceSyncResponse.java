package com.princesses7.findy.shopping.product.external.dto.response;

import java.util.List;

public record KcaMissingPriceSyncResponse(
	int totalCount,
	int matchedCount,
	int reviewRequiredCount,
	int skippedCount,
	int appliedCount,
	int page,
	int size,
	long offset,
	int externalTotalCount,
	boolean hasNext,
	List<KcaMissingPriceSyncItemResponse> items
) {

	public static KcaMissingPriceSyncResponse from(List<KcaMissingPriceSyncItemResponse> items) {
		return from(items, 0, items.size(), items.size());
	}

	public static KcaMissingPriceSyncResponse from(
		List<KcaMissingPriceSyncItemResponse> items,
		int page,
		int size,
		int externalTotalCount
	) {
		int matchedCount = countByStatus(items, "MATCHED");
		int reviewRequiredCount = countByStatus(items, "REVIEW_REQUIRED");
		int skippedCount = countByStatus(items, "REJECTED");
		int appliedCount = (int)items.stream()
			.filter(KcaMissingPriceSyncItemResponse::applied)
			.count();
		long offset = (long)page * size;
		boolean hasNext = offset + items.size() < externalTotalCount;

		return new KcaMissingPriceSyncResponse(
			items.size(),
			matchedCount,
			reviewRequiredCount,
			skippedCount,
			appliedCount,
			page,
			size,
			offset,
			externalTotalCount,
			hasNext,
			items
		);
	}

	private static int countByStatus(List<KcaMissingPriceSyncItemResponse> items, String status) {
		return (int)items.stream()
			.filter(item -> status.equals(item.matchStatus()))
			.count();
	}
}
