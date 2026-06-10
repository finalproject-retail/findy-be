package com.princesses7.findy.analytics.movement.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.global.response.ApiResponse;
import com.princesses7.findy.analytics.movement.dto.response.UserMovementVisualizationResponse;
import com.princesses7.findy.analytics.movement.service.UserMovementVisualizationService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class AdminUserMovementVisualizationController {

	private final UserMovementVisualizationService userMovementVisualizationService;

	@GetMapping("/api/v1/admin/analytics/users/movements")
	public ApiResponse<UserMovementVisualizationResponse> getUserMovementVisualization(
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
		Long userId,

		@RequestParam(required = false)
		@Min(0)
		Integer minStaySeconds,

		@RequestParam(required = false)
		@Min(1)
		Integer limit
	) {
		UserMovementVisualizationResponse response = userMovementVisualizationService.getUserMovementVisualization(
			fromDate,
			toDate,
			storeId,
			userId,
			minStaySeconds,
			limit
		);

		return ApiResponse.ok("사용자 동선 시각화용 데이터 조회에 성공했습니다.", response);
	}
}