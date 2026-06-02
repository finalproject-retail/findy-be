package com.princesses7.findy.user.recentview.dto.response;

import java.util.List;

public record RecentViewProductListResponse(
	int count,
	List<RecentViewProductResponse> recentViews
) {

	public static RecentViewProductListResponse from(List<RecentViewProductResponse> recentViews) {
		return new RecentViewProductListResponse(
			recentViews.size(),
			recentViews
		);
	}
}