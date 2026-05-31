package com.princesses7.findy.shopping.product.dto.response;

import java.util.List;

public record HaccpProductImportItemResponse(
	Long productId,
	String productName,
	String barcode,
	String importStatus,
	List<String> updatedFields
) {
}