package com.princesses7.findy.shopping.product.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductImportResultResponse;
import com.princesses7.findy.shopping.product.service.NaverProductImportService;
import com.princesses7.findy.shopping.product.service.ProductImportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/import")
public class ProductImportController {

	private final NaverProductImportService naverProductImportService;
	private final ProductImportService productImportService;

	@PostMapping("/naver")
	public ProductImportResultResponse importByKeyword(
		@RequestParam String keyword,
		@RequestParam(defaultValue = "10") int display
	) {
		return naverProductImportService.importByKeyword(keyword, display);
	}

	@PostMapping("/mfds")
	public ApiResponse<Long> importProductFromMfds(@RequestParam String barcode) {
		Long productId = productImportService.importByBarcode(barcode);
		return ApiResponse.ok(productId);
	}

	@PostMapping("/haccp")
	public ApiResponse<HaccpProductUpsertResponse> importHaccpProduct(
		@RequestParam String productName
	) {
		return ApiResponse.ok(
			"HACCP 상품 정보 동기화에 성공했습니다.",
			haccpProductUpsertService.upsertByProductName(productName)
		);
	}
}