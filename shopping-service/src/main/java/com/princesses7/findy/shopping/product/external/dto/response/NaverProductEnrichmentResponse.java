package com.princesses7.findy.shopping.product.external.dto.response;

import java.util.List;

public record NaverProductEnrichmentResponse(
	int totalCount,
	int matchedCount,
	int reviewRequiredCount,
	int skippedCount,
	int appliedCount,
	int offset,
	int limit,
	Integer nextOffset,
	boolean hasNext,
	List<NaverProductEnrichmentItemResponse> items
) {

	public static NaverProductEnrichmentResponse from(
		List<NaverProductEnrichmentItemResponse> items,
		int offset,
		int limit,
		boolean hasNext
	) {
		int matchedCount = countByStatus(items, "MATCHED");
		int reviewRequiredCount = countByStatus(items, "REVIEW_REQUIRED");
		int skippedCount = countByStatus(items, "REJECTED");
		int appliedCount = (int)items.stream()
			.filter(NaverProductEnrichmentItemResponse::applied)
			.count();
		Integer nextOffset = hasNext ? offset + items.size() : null;

		return new NaverProductEnrichmentResponse(
			items.size(),
			matchedCount,
			reviewRequiredCount,
			skippedCount,
			appliedCount,
			offset,
			limit,
			nextOffset,
			hasNext,
			items
		);
	}

	private static int countByStatus(List<NaverProductEnrichmentItemResponse> items, String status) {
		return (int)items.stream()
			.filter(item -> status.equals(item.matchStatus()))
			.count();
	}
}
