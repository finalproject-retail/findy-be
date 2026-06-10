package com.princesses7.findy.shopping.product.dto.response;

import com.princesses7.findy.shopping.product.entity.Product;

public record ProductLocationResponse(
	Long productId,
	String productName,
	Long storeId,
	Long gridId
) {

	public static ProductLocationResponse from(Product product, Long storeId) {
		return new ProductLocationResponse(
			product.getProductId(),
			product.getProductName(),
			storeId,
			product.getGridId()
		);
	}
}