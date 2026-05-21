package com.princesses7.findy.shopping.order.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderCreateResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderDetailResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderSummaryResponse;
import com.princesses7.findy.shopping.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/orders")
public class OrderTestController {

	private final OrderService orderService;

	@PostMapping
	public ApiResponse<OrderCreateResponse> createOrder(
		@RequestHeader("X-User-Id") Long userId
	) {
		OrderCreateResponse response = orderService.createOrder(userId);

		return ApiResponse.ok("주문 생성에 성공했습니다.", response);
	}

	@GetMapping
	public ApiResponse<List<OrderSummaryResponse>> getOrders(
		@RequestHeader("X-User-Id") Long userId
	) {
		List<OrderSummaryResponse> response = orderService.getOrders(userId);

		return ApiResponse.ok("주문 목록 조회에 성공했습니다.", response);
	}

	@GetMapping("/{orderId}")
	public ApiResponse<OrderDetailResponse> getOrder(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long orderId
	) {
		OrderDetailResponse response = orderService.getOrder(userId, orderId);

		return ApiResponse.ok("주문 상세 조회에 성공했습니다.", response);
	}
}