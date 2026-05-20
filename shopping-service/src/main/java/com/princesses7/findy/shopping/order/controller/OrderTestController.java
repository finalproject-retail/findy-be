package com.princesses7.findy.shopping.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderCreateResponse;
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
}