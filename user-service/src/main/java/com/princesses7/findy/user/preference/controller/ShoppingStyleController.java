package com.princesses7.findy.user.preference.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.preference.dto.response.ShoppingStyleResponse;
import com.princesses7.findy.user.preference.service.ShoppingStyleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-styles")
public class ShoppingStyleController {

	private final ShoppingStyleService shoppingStyleService;

	@GetMapping
	public ApiResponse<List<ShoppingStyleResponse>> getShoppingStyles() {
		return ApiResponse.ok(
			"쇼핑 스타일 목록 조회에 성공했습니다.",
			shoppingStyleService.getShoppingStyles()
		);
	}
}