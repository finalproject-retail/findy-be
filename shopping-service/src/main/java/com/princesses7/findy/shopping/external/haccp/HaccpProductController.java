package com.princesses7.findy.shopping.external.haccp;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;
import com.princesses7.findy.shopping.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/haccp/products")
public class HaccpProductController {

	private final HaccpProductClient haccpProductClient;

	@GetMapping
	public ApiResponse<List<HaccpProductItemResponse>> search(
		@RequestParam String productName
	) {
		List<HaccpProductItemResponse> response = haccpProductClient.searchByProductName(productName);

		return ApiResponse.ok("HACCP 제품이미지 및 포장지표기정보 조회에 성공했습니다.", response);
	}

	@GetMapping("/raw")
	public String searchRaw(
		@RequestParam String productName
	) {
		return haccpProductClient.getRawByProductName(productName);
	}
}