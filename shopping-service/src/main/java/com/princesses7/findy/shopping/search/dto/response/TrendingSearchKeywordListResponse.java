package com.princesses7.findy.shopping.search.dto.response;

import java.util.List;

public record TrendingSearchKeywordListResponse(
	List<TrendingSearchKeywordResponse> keywords
) {
}