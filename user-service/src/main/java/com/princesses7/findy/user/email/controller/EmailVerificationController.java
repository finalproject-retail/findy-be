package com.princesses7.findy.user.email.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.email.dto.request.SendEmailVerificationRequest;
import com.princesses7.findy.user.email.dto.request.VerifyEmailCodeRequest;
import com.princesses7.findy.user.email.dto.response.SendEmailVerificationResponse;
import com.princesses7.findy.user.email.dto.response.VerifyEmailCodeResponse;
import com.princesses7.findy.user.email.service.EmailVerificationService;
import com.princesses7.findy.user.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/email-verifications")
@RequiredArgsConstructor
public class EmailVerificationController {

	private final EmailVerificationService emailVerificationService;

	@PostMapping
	public ApiResponse<SendEmailVerificationResponse> sendCode(
		@Valid @RequestBody SendEmailVerificationRequest request
	) {
		SendEmailVerificationResponse response = emailVerificationService.sendCode(
			request.email(),
			request.purpose()
		);
		return ApiResponse.ok("이메일 인증 코드 발송에 성공했습니다.", response);
	}

	@PostMapping("/verify")
	public ApiResponse<VerifyEmailCodeResponse> verifyCode(
		@Valid @RequestBody VerifyEmailCodeRequest request
	) {
		VerifyEmailCodeResponse response = emailVerificationService.verifyCode(
			request.email(),
			request.purpose(),
			request.code()
		);
		return ApiResponse.ok("이메일 인증에 성공했습니다.", response);
	}
}
