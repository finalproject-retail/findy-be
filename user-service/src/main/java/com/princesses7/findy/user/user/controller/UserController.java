package com.princesses7.findy.user.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.user.dto.request.SignupRequestDTO;
import com.princesses7.findy.user.user.dto.response.MyPageResponse;
import com.princesses7.findy.user.user.dto.response.SignupResponse;
import com.princesses7.findy.user.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/signup")
	public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequestDTO request) {
		Long userId = userService.signup(request);

		return ApiResponse.ok("회원가입에 성공했습니다.", new SignupResponse(userId));
	}

	@GetMapping("/me")
	public ApiResponse<MyPageResponse> getMyPage(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.ok(
			"마이페이지 정보 조회에 성공했습니다.",
			userService.getMyPage(userId)
		);
	}
}