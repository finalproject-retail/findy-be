package com.princesses7.findy.analytics.product.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.global.response.ApiResponse;
import com.princesses7.findy.analytics.product.dto.response.ProductPerformanceSummaryResponse;
import com.princesses7.findy.analytics.product.service.ProductPerformanceSummaryService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class ProductPerformanceSummaryController {

	private final ProductPerformanceSummaryService productPerformanceSummaryService;

	@GetMapping("/api/v1/admin/analytics/products/performance-summary")
	public ApiResponse<ProductPerformanceSummaryResponse> getProductPerformanceSummary(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate fromDate,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate toDate,

		@RequestParam(required = false)
		Long storeId,

		@RequestParam(required = false)
		@Min(1)
		@Max(100)
		Integer limit
	) {
		ProductPerformanceSummaryResponse response = productPerformanceSummaryService.getProductPerformanceSummary(
			fromDate,
			toDate,
			storeId,
			limit
		);

		return ApiResponse.ok("상품 성과 요약 조회에 성공했습니다.", response);
	}
}