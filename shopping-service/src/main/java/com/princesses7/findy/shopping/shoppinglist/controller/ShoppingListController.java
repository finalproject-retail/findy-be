package com.princesses7.findy.shopping.shoppinglist.controller;

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

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.shoppinglist.dto.request.AddShoppingListItemRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.ChangeShoppingListItemQuantityRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.ScanShoppingListItemRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.response.ShoppingListResponse;
import com.princesses7.findy.shopping.shoppinglist.service.ShoppingListService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-lists")
public class ShoppingListController {

	private final ShoppingListService shoppingListService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ShoppingListResponse> createShoppingList(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.ok(
			"쇼핑리스트가 생성되었습니다.",
			shoppingListService.createShoppingList(userId)
		);
	}

	@GetMapping
	public ApiResponse<ShoppingListResponse> getShoppingList(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.ok(
			shoppingListService.getShoppingList(userId)
		);
	}

	@PostMapping("/items")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ShoppingListResponse> addShoppingListItem(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody AddShoppingListItemRequest request
	) {
		return ApiResponse.ok(
			"쇼핑리스트에 상품이 추가되었습니다.",
			shoppingListService.addShoppingListItem(userId, request)
		);
	}

	@PostMapping("/scan")
	public ApiResponse<ShoppingListResponse> scanShoppingListItem(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody ScanShoppingListItemRequest request
	) {
		return ApiResponse.ok(
			"상품 스캔이 반영되었습니다.",
			shoppingListService.scanShoppingListItem(userId, request)
		);
	}

	@PostMapping("/scan/decrease")
	public ApiResponse<ShoppingListResponse> decreaseShoppingListItemQuantityByScan(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody ScanShoppingListItemRequest request
	) {
		return ApiResponse.ok(
			"스캔 상품 수량이 감소되었습니다.",
			shoppingListService.decreaseShoppingListItemQuantityByScan(userId, request)
		);
	}

	@PatchMapping("/items/{shoppingListItemId}/quantity")
	public ApiResponse<ShoppingListResponse> changeShoppingListItemQuantity(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long shoppingListItemId,
		@Valid @RequestBody ChangeShoppingListItemQuantityRequest request
	) {
		return ApiResponse.ok(
			"쇼핑리스트 상품 수량이 변경되었습니다.",
			shoppingListService.changeShoppingListItemQuantity(
				userId,
				shoppingListItemId,
				request
			)
		);
	}

	@DeleteMapping("/items/{shoppingListItemId}")
	public ApiResponse<ShoppingListResponse> removeShoppingListItem(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long shoppingListItemId
	) {
		return ApiResponse.ok(
			"쇼핑리스트 상품이 삭제되었습니다.",
			shoppingListService.removeShoppingListItem(userId, shoppingListItemId)
		);
	}
}