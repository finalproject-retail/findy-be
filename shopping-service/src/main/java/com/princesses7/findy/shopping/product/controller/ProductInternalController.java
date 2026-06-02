package com.princesses7.findy.shopping.product.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.product.service.ProductSummaryReader;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/products")
public class ProductInternalController {

	private final ProductSummaryReader productSummaryReader;

	@GetMapping("/summaries")
	public ApiResponse<List<ProductSummaryResponse>> getProductSummaries(
		@RequestParam List<Long> productIds
	) {
		return ApiResponse.ok(
			"상품 요약 조회에 성공했습니다.",
			productSummaryReader.getExistingProductSummaries(productIds)
		);
	}
}