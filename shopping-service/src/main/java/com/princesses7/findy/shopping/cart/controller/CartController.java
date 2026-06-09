package com.princesses7.findy.shopping.cart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.cart.dto.request.AddCartItemRequest;
import com.princesses7.findy.shopping.cart.dto.request.ChangeCartItemCheckedRequest;
import com.princesses7.findy.shopping.cart.dto.request.ChangeCartItemQuantityRequest;
import com.princesses7.findy.shopping.cart.dto.response.CartResponse;
import com.princesses7.findy.shopping.cart.service.CartService;
import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.store.ResolvedStoreId;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

	private final CartService cartService;

	@PostMapping("/items")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<CartResponse> addCartItem(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody AddCartItemRequest request,
		@ResolvedStoreId long storeId
	) {
		return ApiResponse.ok(
			"장바구니에 상품이 추가되었습니다.",
			cartService.addCartItem(userId, request, storeId)
		);
	}

	@GetMapping
	public ApiResponse<CartResponse> getCart(
		@RequestHeader("X-User-Id") Long userId,
		@ResolvedStoreId long storeId
	) {
		return ApiResponse.ok(
			cartService.getCart(userId, storeId)
		);
	}

	@PatchMapping("/items/stock-sync")
	public ApiResponse<CartResponse> syncCartItemStocks(
		@RequestHeader("X-User-Id") Long userId,
		@ResolvedStoreId long storeId
	) {
		return ApiResponse.ok(
			"장바구니 상품 재고가 동기화되었습니다.",
			cartService.syncCartItemStocks(userId, storeId)
		);
	}

	@PatchMapping("/items/{cartItemId}/quantity")
	public ApiResponse<CartResponse> changeCartItemQuantity(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long cartItemId,
		@Valid @RequestBody ChangeCartItemQuantityRequest request,
		@ResolvedStoreId long storeId
	) {
		return ApiResponse.ok(
			"장바구니 상품 수량이 변경되었습니다.",
			cartService.changeCartItemQuantity(userId, cartItemId, request, storeId)
		);
	}

	@PatchMapping("/items/{cartItemId}/check")
	public ApiResponse<CartResponse> changeCartItemChecked(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long cartItemId,
		@Valid @RequestBody ChangeCartItemCheckedRequest request,
		@ResolvedStoreId long storeId
	) {
		return ApiResponse.ok(
			"장바구니 상품 선택 상태가 변경되었습니다.",
			cartService.changeCartItemChecked(userId, cartItemId, request, storeId)
		);
	}

	@DeleteMapping("/items/{cartItemId}")
	public ApiResponse<CartResponse> removeCartItem(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long cartItemId,
		@ResolvedStoreId long storeId
	) {
		return ApiResponse.ok(
			"장바구니 상품이 삭제되었습니다.",
			cartService.removeCartItem(userId, cartItemId, storeId)
		);
	}
}
