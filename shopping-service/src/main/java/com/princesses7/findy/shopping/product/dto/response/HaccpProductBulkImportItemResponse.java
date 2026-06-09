package com.princesses7.findy.shopping.product.dto.response;

import java.util.List;

public record HaccpProductBulkImportItemResponse(
	String keyword,
	Long productId,
	String productName,
	String brandName,
	String barcode,
	String importStatus,
	List<String> updatedFields
) {

	public static HaccpProductBulkImportItemResponse from(
		String keyword,
		HaccpProductImportItemResponse item
	) {
		return new HaccpProductBulkImportItemResponse(
			keyword,
			item.productId(),
			item.productName(),
			item.brandName(),
			item.barcode(),
			item.importStatus(),
			item.updatedFields()
		);
	}
}