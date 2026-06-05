package com.princesses7.findy.analytics.inventory.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.global.response.ApiResponse;
import com.princesses7.findy.analytics.inventory.dto.response.StockoutAnalyticsResponse;
import com.princesses7.findy.analytics.inventory.service.StockoutAnalyticsService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class AdminStockoutAnalyticsController {

	private final StockoutAnalyticsService stockoutAnalyticsService;

	@GetMapping("/api/v1/admin/analytics/inventories/stockouts")
	public ApiResponse<StockoutAnalyticsResponse> getStockoutAnalytics(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate fromDate,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate toDate,

		@RequestParam(required = false)
		Long storeId,

		@RequestParam(required = false)
		Long categoryId,

		@RequestParam(required = false)
		@Min(1)
		@Max(100)
		Integer limit
	) {
		StockoutAnalyticsResponse response = stockoutAnalyticsService.getStockoutAnalytics(
			fromDate,
			toDate,
			storeId,
			categoryId,
			limit
		);

		return ApiResponse.ok("품절 발생 현황 분석 조회에 성공했습니다.", response);
	}
}