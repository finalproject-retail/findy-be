package com.princesses7.findy.shopping.product.external.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.product.external.dto.response.KcaMissingPriceAsyncStatusResponse;
import com.princesses7.findy.shopping.product.external.dto.response.KcaMissingPriceSyncResponse;
import com.princesses7.findy.shopping.product.external.service.KcaMissingPriceAsyncService;
import com.princesses7.findy.shopping.product.external.service.KcaMissingPriceSyncService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kca/product-prices")
public class KcaMissingPriceSyncController {

	private final KcaMissingPriceSyncService kcaMissingPriceSyncService;
	private final KcaMissingPriceAsyncService kcaMissingPriceAsyncService;

	@PostMapping("/sync-missing")
	public ApiResponse<KcaMissingPriceSyncResponse> syncMissingPrices(
		@RequestParam(required = false) String goodInspectDay,
		@RequestParam(required = false) String entpId,
		@RequestParam(required = false) String goodId,
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "50") int limit,
		@RequestParam(required = false) Integer maxProducts
	) {
		return ApiResponse.ok(
			"한국소비자원 가격 기반 미입력 상품 가격 보정에 성공했습니다.",
			kcaMissingPriceSyncService.syncMissingPrices(goodInspectDay, entpId, goodId, offset, limit, maxProducts)
		);
	}

	@PostMapping("/sync-missing/async")
	public ApiResponse<KcaMissingPriceAsyncStatusResponse> startSyncMissingPricesAsync(
		@RequestParam(required = false) String goodInspectDay,
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "20") int limit,
		@RequestParam(defaultValue = "1000") long delayMillis
	) {
		return ApiResponse.ok(
			"KCA 가격 보강 작업을 백그라운드에서 시작했습니다.",
			kcaMissingPriceAsyncService.start(goodInspectDay, offset, limit, delayMillis)
		);
	}

	@GetMapping("/sync-missing/async/status")
	public ApiResponse<KcaMissingPriceAsyncStatusResponse> getSyncMissingPricesAsyncStatus() {
		return ApiResponse.ok(kcaMissingPriceAsyncService.getStatus());
	}
}
