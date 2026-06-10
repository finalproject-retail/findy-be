package com.princesses7.findy.shopping.product.external.dto.response;

import java.util.List;

public record KcaMissingPriceSyncResponse(
	int totalCount,
	int matchedCount,
	int reviewRequiredCount,
	int skippedCount,
	int appliedCount,
	String goodInspectDay,
	int sourceItemCount,
	int aggregatedItemCount,
	String kcaResultCode,
	String kcaResultMessage,
	int productInfoCount,
	int processedProductCount,
	int offset,
	int limit,
	Integer nextOffset,
	boolean hasNext,
	List<KcaMissingPriceSyncItemResponse> items
) {

	public static KcaMissingPriceSyncResponse from(List<KcaMissingPriceSyncItemResponse> items) {
		return from(items, null, items.size(), items.size(), null, null, 0, 0, 0, items.size(), null, false);
		List<KcaMissingPriceSyncItemResponse> items,
		String goodInspectDay,
		int sourceItemCount,
		int aggregatedItemCount,
		String kcaResultCode,
		String kcaResultMessage,
		int productInfoCount,
		int processedProductCount,
		int offset,
		int limit,
		Integer nextOffset,
		boolean hasNext
	) {
		int matchedCount = countByStatus(items, "MATCHED");
		int reviewRequiredCount = countByStatus(items, "REVIEW_REQUIRED");
		int skippedCount = countByStatus(items, "REJECTED");
		int appliedCount = (int)items.stream()
			.filter(KcaMissingPriceSyncItemResponse::applied)
			.count();

		return new KcaMissingPriceSyncResponse(
			items.size(),
			matchedCount,
			reviewRequiredCount,
			skippedCount,
			appliedCount,
			goodInspectDay,
			sourceItemCount,
			aggregatedItemCount,
			kcaResultCode,
			kcaResultMessage,
			productInfoCount,
			processedProductCount,
			offset,
			limit,
			nextOffset,
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
