package com.princesses7.findy.shopping.purchase.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseTargetResponse;
import com.princesses7.findy.shopping.purchase.service.PurchaseTargetService;
import com.princesses7.findy.shopping.store.ResolvedStoreId;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/purchase-targets")
public class PurchaseTargetController {

	private final PurchaseTargetService purchaseTargetService;

	@GetMapping
	public ApiResponse<PurchaseTargetResponse> getPurchaseTargets(
		@RequestHeader("X-User-Id") Long userId,
		@ResolvedStoreId long storeId
	) {
		PurchaseTargetResponse response = purchaseTargetService.getPurchaseTargets(userId, storeId);

		return ApiResponse.ok("구매 대상 상품 검증에 성공했습니다.", response);
	}
}