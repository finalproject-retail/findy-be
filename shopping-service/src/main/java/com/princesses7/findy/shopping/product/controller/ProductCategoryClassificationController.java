package com.princesses7.findy.shopping.product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.external.openai.OpenAiCategoryClassifierClient;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/categories/classify")
public class ProductCategoryClassificationController {

	private final OpenAiCategoryClassifierClient classifierClient;

	@GetMapping
	public ProductCategoryClassificationResponse classify(
		@RequestParam String productName,
		@RequestParam(required = false) String brandName,
		@RequestParam(required = false) String externalCategory
	) {
		return classifierClient.classify(productName, brandName, externalCategory);
	}
}