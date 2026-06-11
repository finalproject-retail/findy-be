package com.princesses7.findy.analytics.stay.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.global.response.ApiResponse;
import com.princesses7.findy.analytics.stay.dto.response.ZoneAverageStayTimeAnalyticsResponse;
import com.princesses7.findy.analytics.stay.service.ZoneAverageStayTimeAnalyticsService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class AdminZoneAverageStayTimeAnalyticsController {

	private final ZoneAverageStayTimeAnalyticsService zoneAverageStayTimeAnalyticsService;

	@GetMapping("/api/v1/analytics/zones/average-stay-times")
	public ApiResponse<ZoneAverageStayTimeAnalyticsResponse> getZoneAverageStayTimes(
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
		@Min(0)
		Integer minStaySeconds,

		@RequestParam(required = false)
		@Min(1)
		Integer limit
	) {
		ZoneAverageStayTimeAnalyticsResponse response = zoneAverageStayTimeAnalyticsService.getZoneAverageStayTimes(
			fromDate,
			toDate,
			storeId,
			zoneId,
			minStaySeconds,
			limit
		);

		return ApiResponse.ok("구역별 평균 체류 시간 분석 조회에 성공했습니다.", response);
	}
}