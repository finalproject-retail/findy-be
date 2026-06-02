package com.princesses7.findy.user.recentview.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.user.recentview.entity.RecentViewProduct;

public record RecentViewProductResponse(
	Long recentViewId,
	Long productId,
	LocalDateTime viewedAt,
	ProductSummaryResponse product
) {

	public static RecentViewProductResponse from(
		RecentViewProduct recentViewProduct,
		ProductSummaryResponse product
	) {
		return new RecentViewProductResponse(
			recentViewProduct.getRecentViewId(),
			recentViewProduct.getProductId(),
			recentViewProduct.getViewedAt(),
			product
		);
	}
}