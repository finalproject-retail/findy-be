package com.princesses7.findy.recommendation.preference.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.global.response.ApiResponse;
import com.princesses7.findy.recommendation.preference.dto.response.UserPreferenceResponse;
import com.princesses7.findy.recommendation.preference.service.UserPreferenceQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/preferences")
public class UserPreferenceController {

	private final UserPreferenceQueryService userPreferenceQueryService;

	@GetMapping
	public ApiResponse<UserPreferenceResponse> getUserPreference(
		@RequestParam Long userId
	) {
		UserPreferenceResponse response = userPreferenceQueryService.getUserPreference(userId);

		return ApiResponse.ok("사용자 선호 정보 조회에 성공했습니다.", response);
	}
}