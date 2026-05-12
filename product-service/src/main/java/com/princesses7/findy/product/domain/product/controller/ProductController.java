package com.princesses7.findy.product.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.product.domain.product.dto.response.ProductPageResponse;
import com.princesses7.findy.product.domain.product.service.ProductService;
import com.princesses7.findy.product.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public ApiResponse<ProductPageResponse> getProducts(
			@RequestParam(required = false) Long categoryId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			// 인기순 정렬은 Redis 랭킹 데이터 연동 시 별도 구현 예정
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction
	) {
		ProductPageResponse response = productService.getProducts(
				categoryId,
				page,
				size,
				sortBy,
				direction
		);

		return ApiResponse.ok(response);
	}
}
