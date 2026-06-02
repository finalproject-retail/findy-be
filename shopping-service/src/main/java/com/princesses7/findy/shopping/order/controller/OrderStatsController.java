package com.princesses7.findy.shopping.order.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.order.dto.response.FrequentPurchaseProductListResponse;
import com.princesses7.findy.shopping.order.service.OrderStatsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders/stats")
public class OrderStatsController {

	private final OrderStatsService orderStatsService;

	@GetMapping("/frequent-products")
	public ApiResponse<FrequentPurchaseProductListResponse> getFrequentPurchaseProducts(
		@RequestHeader(value = "X-USER-ID", required = false) Long userId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
		@RequestParam(required = false) Integer limit
	) {
		FrequentPurchaseProductListResponse response = orderStatsService.getFrequentPurchaseProducts(
			userId,
			fromDate,
			toDate,
			limit
		);

		return ApiResponse.ok("자주 산 상품 조회에 성공했습니다.", response);
	}
}