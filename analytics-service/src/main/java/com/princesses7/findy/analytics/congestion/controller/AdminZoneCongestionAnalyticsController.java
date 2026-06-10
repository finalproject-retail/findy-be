package com.princesses7.findy.analytics.congestion.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.congestion.dto.response.ZoneCongestionAnalyticsResponse;
import com.princesses7.findy.analytics.congestion.service.ZoneCongestionAnalyticsService;
import com.princesses7.findy.analytics.global.response.ApiResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class AdminZoneCongestionAnalyticsController {

	private final ZoneCongestionAnalyticsService zoneCongestionAnalyticsService;

	@GetMapping("/api/v1/admin/analytics/zones/congestion")
	public ApiResponse<ZoneCongestionAnalyticsResponse> getZoneCongestion(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate fromDate,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate toDate,

		@RequestParam
		@NotNull
		Long storeId,

		@RequestParam(required = false)
		Long zoneId,

		@RequestParam(required = false)
		@Min(1)
		@Max(60)
		Integer intervalMinutes,

		@RequestParam(required = false)
		@Min(1)
		Integer limit
	) {
		ZoneCongestionAnalyticsResponse response = zoneCongestionAnalyticsService.getZoneCongestion(
			fromDate,
			toDate,
			storeId,
			zoneId,
			intervalMinutes,
			limit
		);

		return ApiResponse.ok("구역별 혼잡도 분석 조회에 성공했습니다.", response);
	}
}