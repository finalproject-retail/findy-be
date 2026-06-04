package com.princesses7.findy.shopping.product.external.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.product.external.dto.response.KcaMissingPriceSyncResponse;
import com.princesses7.findy.shopping.product.external.service.KcaMissingPriceSyncService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kca/product-prices")
public class KcaMissingPriceSyncController {

	private final KcaMissingPriceSyncService kcaMissingPriceSyncService;

	@PostMapping("/sync-missing")
	public ApiResponse<KcaMissingPriceSyncResponse> syncMissingPrices(
		@RequestParam String goodInspectDay,
		@RequestParam(required = false) String entpId,
		@RequestParam(required = false) String goodId,
		@RequestParam(defaultValue = "50") int limit
	) {
		return ApiResponse.ok(
			"한국소비자원 가격 기반 미입력 상품 가격 보정에 성공했습니다.",
			kcaMissingPriceSyncService.syncMissingPrices(goodInspectDay, entpId, goodId, limit)
		);
	}
}