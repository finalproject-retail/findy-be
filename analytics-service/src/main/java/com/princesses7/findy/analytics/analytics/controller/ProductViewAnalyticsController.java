package com.princesses7.findy.analytics.analytics.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.analytics.dto.response.ProductViewAnalyticsResponse;
import com.princesses7.findy.analytics.analytics.service.ProductViewAnalyticsService;
import com.princesses7.findy.analytics.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics/products")
public class ProductViewAnalyticsController {

	private final ProductViewAnalyticsService productViewAnalyticsService;

	@GetMapping("/views")
	public ApiResponse<ProductViewAnalyticsResponse> getProductViewAnalytics(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate fromDate,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate toDate,

		@RequestParam(required = false)
		Integer limit
	) {
		ProductViewAnalyticsResponse response = productViewAnalyticsService.getProductViewAnalytics(
			fromDate,
			toDate,
			limit
		);

		return ApiResponse.ok("상품 조회수 분석 조회에 성공했습니다.", response);
	}
}