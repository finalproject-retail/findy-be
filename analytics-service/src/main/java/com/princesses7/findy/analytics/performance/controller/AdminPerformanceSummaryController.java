package com.princesses7.findy.analytics.performance.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.global.response.ApiResponse;
import com.princesses7.findy.analytics.performance.dto.response.PerformancePeriodSummaryResponse;
import com.princesses7.findy.analytics.performance.dto.response.PerformanceSummaryResponse;
import com.princesses7.findy.analytics.performance.service.PerformanceSummaryService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({
	"/api/v1/admin/analytics",
	"/api/v1/analytics"
})
public class AdminPerformanceSummaryController {

	private final PerformanceSummaryService performanceSummaryService;

	@GetMapping("/performance-summary")
	public ApiResponse<PerformanceSummaryResponse> getSummary(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate startDate,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate endDate,

		@RequestParam(required = false)
		Long storeId
	) {
		PerformanceSummaryResponse response = performanceSummaryService.getSummary(
			startDate,
			endDate,
			storeId
		);

		return ApiResponse.ok("성과 요약 조회에 성공했습니다.", response);
	}

	@GetMapping("/performance-summary/period")
	public ApiResponse<PerformancePeriodSummaryResponse> getPeriodSummary(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate startDate,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate endDate,

		@RequestParam(required = false)
		Long storeId
	) {
		PerformancePeriodSummaryResponse response = performanceSummaryService.getPeriodSummary(
			startDate,
			endDate,
			storeId
		);

		return ApiResponse.ok("기간별 성과 요약 조회에 성공했습니다.", response);
	}
}