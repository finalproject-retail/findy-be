package com.princesses7.findy.shopping.product.dto.request;

import java.util.List;

public record HaccpProductBulkImportRequest(
	List<String> keywords,
	Integer limitPerKeyword,
	Boolean onlyBarcodeExists,
	Boolean createIfMissing
) {

	public int resolvedLimitPerKeyword() {
		if (limitPerKeyword == null || limitPerKeyword <= 0) {
			return 10;
		}

		return limitPerKeyword;
	}

	public boolean resolvedOnlyBarcodeExists() {
		return onlyBarcodeExists == null || onlyBarcodeExists;
	}

	public boolean resolvedCreateIfMissing() {
		return createIfMissing == null || createIfMissing;
	}
}