package com.princesses7.findy.shopping.purchase.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseAmountResponse;
import com.princesses7.findy.shopping.purchase.service.PurchaseAmountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/purchase-amounts")
public class PurchaseAmountController {

	private final PurchaseAmountService purchaseAmountService;

	@GetMapping
	public ApiResponse<PurchaseAmountResponse> calculate(
		@RequestHeader("X-User-Id") Long userId
	) {
		PurchaseAmountResponse response = purchaseAmountService.calculate(userId);

		return ApiResponse.ok("구매 금액 계산에 성공했습니다.", response);
	}
}