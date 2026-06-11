package com.princesses7.findy.analytics.zone.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.global.response.ApiResponse;
import com.princesses7.findy.analytics.zone.dto.response.ZoneVisitRateAnalyticsResponse;
import com.princesses7.findy.analytics.zone.service.ZoneVisitRateAnalyticsService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class AdminZoneVisitRateAnalyticsController {

	private final ZoneVisitRateAnalyticsService zoneVisitRateAnalyticsService;

	@GetMapping("/api/v1/analytics/zones/visit-rates")
	public ApiResponse<ZoneVisitRateAnalyticsResponse> getZoneVisitRates(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate startDate,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate endDate,

		@RequestParam
		@NotNull
		Long storeId,

		@RequestParam(required = false)
		Long zoneId,

		@RequestParam(required = false)
		@Min(0)
		Integer minStaySeconds,

		@RequestParam(required = false)
		Boolean includeMovement
	) {
		ZoneVisitRateAnalyticsResponse response = zoneVisitRateAnalyticsService.getZoneVisitRates(
			startDate,
			endDate,
			storeId,
			zoneId,
			minStaySeconds,
			includeMovement
		);

		return ApiResponse.ok("구역별 방문율 분석 조회에 성공했습니다.", response);
	}
}