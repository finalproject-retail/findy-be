package com.princesses7.findy.shopping.product.dto.response;

import java.util.List;

public record HaccpProductBulkImportResponse(
	int keywordCount,
	int createdCount,
	int updatedCount,
	int skippedCount,
	List<HaccpProductBulkImportItemResponse> items
) {
}