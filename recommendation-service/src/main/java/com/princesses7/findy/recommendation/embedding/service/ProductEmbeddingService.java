package com.princesses7.findy.recommendation.embedding.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.embedding.dto.response.ProductEmbeddingBatchResponse;
import com.princesses7.findy.recommendation.embedding.dto.response.ProductEmbeddingResponse;
import com.princesses7.findy.recommendation.embedding.entity.ProductEmbedding;
import com.princesses7.findy.recommendation.embedding.repository.ProductEmbeddingRepository;
import com.princesses7.findy.recommendation.external.embedding.ProductEmbeddingClient;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;
import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;
import com.princesses7.findy.recommendation.preference.repository.CategorySnapshotRepository;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.product.repository.ProductSnapshotRepository;
import com.princesses7.findy.recommendation.recommendation.validator.RecommendationRequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductEmbeddingService {

	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_BATCH_SIZE = 100;

	private final ProductSnapshotRepository productRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final ProductEmbeddingClient productEmbeddingClient;
	private final RecommendationRequestValidator requestValidator;

	@Transactional
	public ProductEmbeddingResponse createOrUpdateProductEmbedding(Long productId) {
		requestValidator.validatePositiveId(productId, "productId");

		ProductSnapshot product = productRepository.findById(productId)
			.orElseThrow(() -> new BaseException(ErrorCode.RECOMMENDATION_PRODUCT_NOT_FOUND));

		ProductEmbedding productEmbedding = saveProductEmbedding(product);

		return new ProductEmbeddingResponse(
			productEmbedding.getProductId(),
			productEmbedding.getModel(),
			productEmbedding.getDimensions()
		);
	}

	@Transactional
	public ProductEmbeddingBatchResponse createOrUpdateProductEmbeddings(int limit) {
		int normalizedLimit = normalizeLimit(limit);

		List<ProductSnapshot> products = productRepository.findByDeletedFalse(
			PageRequest.of(0, normalizedLimit)
		);

		int savedCount = 0;

		for (ProductSnapshot product : products) {
			if (!product.isRecommendable()) {
				continue;
			}

			saveProductEmbedding(product);
			savedCount++;
		}

		return new ProductEmbeddingBatchResponse(
			products.size(),
			savedCount
		);
	}

	private ProductEmbedding saveProductEmbedding(ProductSnapshot product) {
		String categoryName = categoryRepository.findById(product.getCategoryId())
			.map(CategorySnapshot::getCategoryName)
			.orElse("");

		List<Double> embedding = productEmbeddingClient.createEmbedding(
			product.toEmbeddingText(categoryName)
		);

		ProductEmbedding productEmbedding = productEmbeddingRepository.findByProductId(product.getProductId())
			.orElseGet(() -> ProductEmbedding.create(
				product.getProductId(),
				productEmbeddingClient.model(),
				productEmbeddingClient.dimensions(),
				embedding
			));

		productEmbedding.update(
			productEmbeddingClient.model(),
			productEmbeddingClient.dimensions(),
			embedding
		);

		return productEmbeddingRepository.save(productEmbedding);
	}

	private int normalizeLimit(int limit) {
		return requestValidator.normalizeSize(limit, DEFAULT_LIMIT, MAX_BATCH_SIZE);
	}
}