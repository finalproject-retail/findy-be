package com.princesses7.findy.shopping.product.dto.response;

public record ProductImportResultResponse(
	int importedCount,
	int skippedCount
) {
}