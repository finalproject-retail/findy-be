package com.princesses7.findy.shopping.order.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.order.dto.request.CreateOrderRequest;
import com.princesses7.findy.shopping.order.dto.response.OrderCreateResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderDetailResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderSummaryResponse;
import com.princesses7.findy.shopping.order.service.OrderService;
import com.princesses7.findy.shopping.store.ResolvedStoreId;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ApiResponse<OrderCreateResponse> createOrder(
		@RequestHeader("X-USER-ID") Long userId,
		@RequestBody(required = false) CreateOrderRequest request,
		@ResolvedStoreId long storeId
	) {
		Long userCouponId = request == null ? null : request.userCouponId();
		Integer usedReward = request == null ? null : request.usedReward();

		OrderCreateResponse response = orderService.createOrder(
			userId,
			userCouponId,
			usedReward,
			storeId
		);

		return ApiResponse.ok("주문 생성에 성공했습니다.", response);
	}

	@GetMapping
	public ApiResponse<List<OrderSummaryResponse>> getOrders(
		@RequestHeader("X-USER-ID") Long userId,
		@ResolvedStoreId long storeId
	) {
		return ApiResponse.ok(
			"주문 목록 조회에 성공했습니다.",
			orderService.getOrders(userId, storeId)
		);
	}

	@GetMapping("/{orderId}")
	public ApiResponse<OrderDetailResponse> getOrder(
		@RequestHeader("X-USER-ID") Long userId,
		@PathVariable Long orderId,
		@ResolvedStoreId long storeId
	) {
		return ApiResponse.ok(
			"주문 상세 조회에 성공했습니다.",
			orderService.getOrder(userId, orderId, storeId)
		);
	}
}