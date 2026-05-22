package com.princesses7.findy.user.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.user.dto.request.SignupRequestDTO;
import com.princesses7.findy.user.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/signup")
	public ApiResponse<Void> signup(@Valid @RequestBody SignupRequestDTO request) {
		userService.signup(request);

		return ApiResponse.ok("회원가입에 성공했습니다.");
	}
}