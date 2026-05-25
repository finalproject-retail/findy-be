package com.princesses7.findy.user.preference.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.preference.dto.request.SavePreferenceRequest;
import com.princesses7.findy.user.preference.dto.response.UserPreferenceResponse;
import com.princesses7.findy.user.preference.service.UserPreferenceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/preferences")
public class UserPreferenceController {

	private final UserPreferenceService userPreferenceService;

	@PostMapping
	public ApiResponse<Void> savePreferences(
		@RequestHeader("X-USER-ID") Long userId,
		@RequestBody SavePreferenceRequest request
	) {
		userPreferenceService.savePreferences(userId, request);

		return ApiResponse.ok("온보딩 선호 정보 저장에 성공했습니다.");
	}

	@GetMapping
	public ApiResponse<UserPreferenceResponse> getMyPreferences(
		@RequestHeader("X-USER-ID") Long userId
	) {
		return ApiResponse.ok(
			"내 선호 정보 조회에 성공했습니다.",
			userPreferenceService.getMyPreferences(userId)
		);
	}
}