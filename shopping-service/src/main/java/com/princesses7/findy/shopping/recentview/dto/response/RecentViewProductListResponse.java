package com.princesses7.findy.shopping.recentview.dto.response;

import java.util.List;

public record RecentViewProductListResponse(
	int count,
	List<RecentViewProductResponse> products
) {

	public static RecentViewProductListResponse from(List<RecentViewProductResponse> products) {
		return new RecentViewProductListResponse(products.size(), products);
	}
}