package com.princesses7.findy.user.social.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.social.dto.request.SocialLoginRequest;
import com.princesses7.findy.user.social.dto.response.SocialLoginResponse;
import com.princesses7.findy.user.social.entity.SocialProvider;
import com.princesses7.findy.user.social.service.SocialLoginService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth/social")
@RequiredArgsConstructor
public class SocialAuthController {

	private final SocialLoginService socialLoginService;

	@PostMapping("/{provider}")
	public ApiResponse<SocialLoginResponse> socialLogin(
		@PathVariable String provider,
		@Valid @RequestBody SocialLoginRequest request
	) {
		SocialLoginResponse response = socialLoginService.login(SocialProvider.from(provider), request);
		return ApiResponse.ok("소셜 로그인에 성공했습니다.", response);
	}
}
