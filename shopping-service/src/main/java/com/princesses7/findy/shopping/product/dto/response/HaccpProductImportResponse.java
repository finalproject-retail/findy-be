package com.princesses7.findy.shopping.product.dto.response;

import java.util.List;

public record HaccpProductImportResponse(
	int importedCount,
	int updatedCount,
	int skippedCount,
	List<HaccpProductImportItemResponse> items
) {
}