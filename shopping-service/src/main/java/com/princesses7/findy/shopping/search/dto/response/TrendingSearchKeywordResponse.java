package com.princesses7.findy.shopping.search.dto.response;

public record TrendingSearchKeywordResponse(
	int rank,
	String keyword,
	long score
) {
}