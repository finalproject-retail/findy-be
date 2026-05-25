package com.princesses7.findy.user.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
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
}
