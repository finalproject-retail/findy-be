package com.princesses7.findy.shopping.product.external.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.product.external.dto.response.NaverProductEnrichmentAsyncStatusResponse;
import com.princesses7.findy.shopping.product.external.dto.response.NaverProductEnrichmentResponse;
import com.princesses7.findy.shopping.product.external.service.NaverProductEnrichmentAsyncService;
import com.princesses7.findy.shopping.product.external.service.NaverProductEnrichmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/enrich/naver")
public class NaverProductEnrichmentController {

	private final NaverProductEnrichmentService enrichmentService;
	private final NaverProductEnrichmentAsyncService asyncService;

	@PostMapping
	public ApiResponse<NaverProductEnrichmentResponse> enrichMissingProducts(
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "20") int limit
	) {
		return ApiResponse.ok(
			"네이버 상품 정보 기반 기존 상품 보강에 성공했습니다.",
			enrichmentService.enrichMissingProducts(offset, limit)
		);
	}

	@PostMapping("/async")
	public ApiResponse<NaverProductEnrichmentAsyncStatusResponse> startAsync(
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "20") int limit,
		@RequestParam(defaultValue = "1000") long delayMillis
	) {
		return ApiResponse.ok(
			"네이버 상품 보강 작업을 백그라운드에서 시작했습니다.",
			asyncService.start(offset, limit, delayMillis)
		);
	}

	@GetMapping("/async/status")
	public ApiResponse<NaverProductEnrichmentAsyncStatusResponse> getAsyncStatus() {
		return ApiResponse.ok(asyncService.getStatus());
	}
}
