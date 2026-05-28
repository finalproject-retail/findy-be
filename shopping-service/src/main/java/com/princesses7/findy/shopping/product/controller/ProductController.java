package com.princesses7.findy.shopping.product.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductDetailResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductPageResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductResponse;
import com.princesses7.findy.shopping.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public ApiResponse<ProductPageResponse> getProducts(
		@RequestParam(required = false) Long categoryId,
		@RequestParam(required = false) String keyword,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		// TODO: 인기순 정렬은 Redis 랭킹 데이터 연동 시 별도 구현 예정
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "desc") String direction
	) {
		ProductPageResponse response = productService.getProducts(
			categoryId,
			keyword,
			page,
			size,
			sortBy,
			direction
		);

		return ApiResponse.ok(response);
	}

	@GetMapping("/new")
	public ApiResponse<List<ProductResponse>> getNewProducts(
		@RequestParam(defaultValue = "10") int size
	) {
		return ApiResponse.ok(
			"신상품 조회에 성공했습니다.",
			productService.getNewProducts(size)
		);
	}

	@GetMapping("/popular")
	public ApiResponse<List<ProductResponse>> getPopularProducts(
		@RequestParam(defaultValue = "10") int size
	) {
		return ApiResponse.ok(
			"인기상품 조회에 성공했습니다.",
			productService.getPopularProducts(size)
		);
	}

	@GetMapping("/mart-recommendations")
	public ApiResponse<List<ProductResponse>> getMartRecommendedProducts(
		@RequestParam(defaultValue = "10") int size
	) {
		return ApiResponse.ok(
			"마트 추천 상품 조회에 성공했습니다.",
			productService.getMartRecommendedProducts(size)
		);
	}

	@GetMapping("/{productId}")
	public ApiResponse<ProductDetailResponse> getProductDetail(
		@PathVariable Long productId
	) {
		ProductDetailResponse response = productService.getProductDetail(productId);

		return ApiResponse.ok("상품 상세 조회에 성공했습니다.", response);
	}
}