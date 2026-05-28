package com.princesses7.findy.shopping.admin.product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.admin.product.dto.response.AdminProductDetailResponse;
import com.princesses7.findy.shopping.admin.product.dto.response.AdminProductPageResponse;
import com.princesses7.findy.shopping.admin.product.service.AdminProductService;
import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

	private final AdminProductService adminProductService;

	@GetMapping
	public ApiResponse<AdminProductPageResponse> getProducts(
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) Long categoryId,
		@RequestParam(required = false) SaleStatus saleStatus,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "desc") String direction
	) {
		return ApiResponse.ok(
			"관리자 상품 목록 조회에 성공했습니다.",
			adminProductService.getProducts(
				keyword,
				categoryId,
				saleStatus,
				page,
				size,
				sortBy,
				direction
			)
		);
	}

	@GetMapping("/{productId}")
	public ApiResponse<AdminProductDetailResponse> getProductDetail(
		@PathVariable Long productId
	) {
		return ApiResponse.ok(
			"관리자 상품 상세 조회에 성공했습니다.",
			adminProductService.getProductDetail(productId)
		);
	}
}