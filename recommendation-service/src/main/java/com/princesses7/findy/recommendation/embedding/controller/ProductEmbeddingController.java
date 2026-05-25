package com.princesses7.findy.recommendation.embedding.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.embedding.dto.response.ProductEmbeddingBatchResponse;
import com.princesses7.findy.recommendation.embedding.dto.response.ProductEmbeddingResponse;
import com.princesses7.findy.recommendation.embedding.service.ProductEmbeddingService;
import com.princesses7.findy.recommendation.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductEmbeddingController {

	private final ProductEmbeddingService productEmbeddingService;

	@PostMapping("/api/v1/recommendations/embeddings/products/{productId}")
	public ApiResponse<ProductEmbeddingResponse> createProductEmbedding(
		@PathVariable Long productId
	) {
		ProductEmbeddingResponse response = productEmbeddingService.createOrUpdateProductEmbedding(productId);

		return ApiResponse.ok("상품 임베딩 생성에 성공했습니다.", response);
	}

	@PostMapping("/api/v1/recommendations/embeddings/products")
	public ApiResponse<ProductEmbeddingBatchResponse> createProductEmbeddings(
		@RequestParam(defaultValue = "20") int limit
	) {
		ProductEmbeddingBatchResponse response = productEmbeddingService.createOrUpdateProductEmbeddings(limit);

		return ApiResponse.ok("상품 임베딩 일괄 생성에 성공했습니다.", response);
	}
}