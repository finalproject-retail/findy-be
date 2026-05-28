package com.princesses7.findy.shopping.search.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.search.dto.response.TrendingSearchKeywordListResponse;
import com.princesses7.findy.shopping.search.service.SearchKeywordRankingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search-keywords")
public class SearchKeywordController {

	private final SearchKeywordRankingService searchKeywordRankingService;

	@GetMapping("/trending")
	public ApiResponse<TrendingSearchKeywordListResponse> getTrendingKeywords(
		@RequestParam(defaultValue = "10") Integer limit
	) {
		return ApiResponse.ok(
			"실시간 인기 검색어 조회에 성공했습니다.",
			new TrendingSearchKeywordListResponse(
				searchKeywordRankingService.getTrendingKeywords(limit)
			)
		);
	}
}