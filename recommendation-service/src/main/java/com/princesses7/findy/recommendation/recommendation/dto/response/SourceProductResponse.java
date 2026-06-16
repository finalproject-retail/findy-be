package com.princesses7.findy.recommendation.recommendation.dto.response;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;

public record SourceProductResponse(
	Long productId,
	String productName,
	String brandName,
	String imageUrl,
	Long categoryId,
	String categoryName,
	Integer originalPrice,
	String saleStatus
) {

	public static SourceProductResponse from(
		ProductSnapshot product,
		String categoryName
	) {
		return new SourceProductResponse(
			product.getProductId(),
			product.getProductName(),
			product.getBrandName(),
			product.getImageUrl(),
			product.getCategoryId(),
			categoryName,
			product.getOriginalPrice(),
			product.getSaleStatus()
		);
	}
}