package com.princesses7.findy.shopping.external.naver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/naver-shopping")
public class NaverShoppingController {

	private final NaverShoppingClient naverShoppingClient;

	@GetMapping
	public NaverShoppingResponse search(
		@RequestParam String query,
		@RequestParam(defaultValue = "5") int display,
		@RequestParam(defaultValue = "1") int start
	) {
		return naverShoppingClient.search(query, display, start);
	}
}