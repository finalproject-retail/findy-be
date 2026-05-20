package com.princesses7.findy.shopping.product.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.product.dto.response.ProductImportResultResponse;
import com.princesses7.findy.shopping.product.service.NaverProductImportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/products/import/naver")
public class NaverProductImportTestController {

	private final NaverProductImportService naverProductImportService;

	@PostMapping
	public ProductImportResultResponse importByKeyword(
		@RequestParam String keyword,
		@RequestParam(defaultValue = "10") int display
	) {
		return naverProductImportService.importByKeyword(keyword, display);
	}
}