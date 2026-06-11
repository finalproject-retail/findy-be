package com.princesses7.findy.user.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.email.dto.response.SendEmailVerificationResponse;
import com.princesses7.findy.user.email.dto.response.VerifyEmailCodeResponse;
import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.password.dto.request.ResetPasswordRequest;
import com.princesses7.findy.user.password.dto.request.SendPasswordResetCodeRequest;
import com.princesses7.findy.user.password.dto.request.VerifyPasswordResetCodeRequest;
import com.princesses7.findy.user.password.service.PasswordResetService;
import com.princesses7.findy.user.user.dto.request.LoginRequestDTO;
import com.princesses7.findy.user.user.dto.response.TokenResponseDTO;
import com.princesses7.findy.user.user.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final PasswordResetService passwordResetService;

	@PostMapping("/login")
	public ApiResponse<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
		TokenResponseDTO response = authService.login(request);
		return ApiResponse.ok(response);
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout() {
		authService.logout();
		return ApiResponse.ok();
	}

	@PostMapping("/password-reset/verification-code")
	public ApiResponse<SendEmailVerificationResponse> sendPasswordResetCode(
		@Valid @RequestBody SendPasswordResetCodeRequest request
	) {
		SendEmailVerificationResponse response = passwordResetService.sendPasswordResetCode(request.email());
		return ApiResponse.ok("비밀번호 찾기 인증 코드 발송에 성공했습니다.", response);
	}

	@PostMapping("/password-reset/verification-code/verify")
	public ApiResponse<VerifyEmailCodeResponse> verifyPasswordResetCode(
		@Valid @RequestBody VerifyPasswordResetCodeRequest request
	) {
		VerifyEmailCodeResponse response = passwordResetService.verifyPasswordResetCode(
			request.email(),
			request.code()
		);
		return ApiResponse.ok("비밀번호 찾기 이메일 인증에 성공했습니다.", response);
	}

	@PostMapping("/password-reset")
	public ApiResponse<Void> resetPassword(
		@Valid @RequestBody ResetPasswordRequest request
	) {
		passwordResetService.resetPassword(
			request.email(),
			request.newPassword(),
			request.newPasswordConfirm()
		);
		return ApiResponse.ok("비밀번호 변경에 성공했습니다.");
	}
}

