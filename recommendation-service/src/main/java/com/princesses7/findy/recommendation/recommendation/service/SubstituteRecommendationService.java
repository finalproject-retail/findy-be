package com.princesses7.findy.recommendation.recommendation.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.embedding.entity.ProductEmbedding;
import com.princesses7.findy.recommendation.embedding.repository.ProductEmbeddingRepository;
import com.princesses7.findy.recommendation.embedding.util.VectorSimilarityCalculator;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;
import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;
import com.princesses7.findy.recommendation.inventory.repository.InventorySnapshotRepository;
import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;
import com.princesses7.findy.recommendation.preference.repository.CategorySnapshotRepository;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.product.repository.ProductSnapshotRepository;
import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionStatus;
import com.princesses7.findy.recommendation.promotion.repository.PromotionProductSnapshotRepository;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.SourceInventoryResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.SourceProductResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.SubstituteRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.support.RecommendationResultPolicy;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;
import com.princesses7.findy.recommendation.recommendation.validator.RecommendationRequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubstituteRecommendationService {

	private static final int DEFAULT_SIZE = 5;
	private static final int MAX_SIZE = 20;

	private final ProductSnapshotRepository productRepository;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final InventorySnapshotRepository inventoryRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final RecommendationRequestValidator requestValidator;
	private final PromotionProductSnapshotRepository promotionProductRepository;

	public SubstituteRecommendationResponse getSubstituteRecommendations(
		Long userId,
		Long productId,
		Long storeId,
		int size,
		boolean force
	) {
		requestValidator.validatePositiveId(userId, "userId");
		requestValidator.validatePositiveId(productId, "productId");
		requestValidator.validatePositiveId(storeId, "storeId");
		int normalizedSize = normalizeSize(size);

		ProductSnapshot sourceProduct = productRepository.findById(productId)
			.orElseThrow(() -> new BaseException(ErrorCode.RECOMMENDATION_PRODUCT_NOT_FOUND));

		String sourceCategoryName = categoryRepository.findById(sourceProduct.getCategoryId())
			.map(CategorySnapshot::getCategoryName)
			.orElse("");

		Optional<InventorySnapshot> sourceInventory = inventoryRepository.findByProductIdAndStoreId(
			productId,
			storeId
		);

		if (sourceInventory.isEmpty()) {
			return emptyResponse(
				userId,
				storeId,
				sourceProduct,
				sourceCategoryName,
				null
			);
		}

		if (!force && !sourceInventory.get().needsSubstituteRecommendation()) {
			return emptyResponse(
				userId,
				storeId,
				sourceProduct,
				sourceCategoryName,
				sourceInventory.get()
			);
		}

		List<ProductSnapshot> candidateProducts = productRepository.findByCategoryIdAndDeletedAtIsNull(
				sourceProduct.getCategoryId()
			)
			.stream()
			.filter(RecommendationResultPolicy::isDisplayableProduct)
			.filter(candidate -> RecommendationResultPolicy.isDifferentProduct(
				candidate,
				sourceProduct.getProductId()
			))
			.toList();

		if (candidateProducts.isEmpty()) {
			return emptyResponse(
				userId,
				storeId,
				sourceProduct,
				sourceCategoryName,
				sourceInventory.get()
			);
		}

		Map<Long, InventorySnapshot> inventoryMap = findAvailableInventoryMap(
			storeId,
			candidateProducts.stream()
				.map(ProductSnapshot::getProductId)
				.toList()
		);

		if (inventoryMap.isEmpty()) {
			return emptyResponse(
				userId,
				storeId,
				sourceProduct,
				sourceCategoryName,
				sourceInventory.get()
			);
		}

		List<Long> promotionTargetProductIds = Stream.concat(
				Stream.of(sourceProduct.getProductId()),
				candidateProducts.stream().map(ProductSnapshot::getProductId)
			)
			.distinct()
			.toList();

		Map<Long, PromotionProductSnapshot> promotionProductMap = findActivePromotionProductMap(
			promotionTargetProductIds
		);

		PromotionProductSnapshot sourcePromotionProduct = promotionProductMap.get(sourceProduct.getProductId());

		Map<Long, ProductEmbedding> embeddingMap = findEmbeddingMap(
			sourceProduct.getProductId(),
			candidateProducts.stream()
				.map(ProductSnapshot::getProductId)
				.toList()
		);

		ProductEmbedding sourceEmbedding = embeddingMap.get(sourceProduct.getProductId());

		List<ProductRecommendationResponse> recommendations = RecommendationResultPolicy.finalizeProductRecommendations(
			candidateProducts.stream()
				.filter(candidate -> inventoryMap.containsKey(candidate.getProductId()))
				.map(candidate -> ProductRecommendationResponse.from(
					candidate,
					promotionProductMap.get(candidate.getProductId()),
					calculateScore(
						sourceProduct,
						sourcePromotionProduct,
						sourceEmbedding,
						candidate,
						promotionProductMap.get(candidate.getProductId()),
						embeddingMap.get(candidate.getProductId()),
						inventoryMap.get(candidate.getProductId())
					),
					RecommendationType.SUBSTITUTE,
					createReason(sourceInventory.get(), candidate, force)
				))
				.toList(),
			normalizedSize
		);

		return new SubstituteRecommendationResponse(
			userId,
			storeId,
			sourceProduct.getProductId(),
			SourceProductResponse.from(sourceProduct, sourceCategoryName),
			SourceInventoryResponse.from(sourceInventory.get()),
			RecommendationType.SUBSTITUTE,
			recommendations
		);
	}

	private Map<Long, InventorySnapshot> findAvailableInventoryMap(
		Long storeId,
		Collection<Long> productIds
	) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return inventoryRepository.findByStoreIdAndProductIdIn(storeId, productIds)
			.stream()
			.filter(InventorySnapshot::hasAvailableStock)
			.collect(Collectors.toMap(
				InventorySnapshot::getProductId,
				inventory -> inventory,
				(left, right) -> left
			));
	}

	private Map<Long, ProductEmbedding> findEmbeddingMap(
		Long sourceProductId,
		Collection<Long> candidateProductIds
	) {
		List<Long> productIds = Stream.concat(
				Stream.of(sourceProductId),
				candidateProductIds.stream()
			)
			.distinct()
			.toList();

		if (productIds.isEmpty()) {
			return Map.of();
		}

		return productEmbeddingRepository.findByProductIdIn(productIds)
			.stream()
			.collect(Collectors.toMap(
				ProductEmbedding::getProductId,
				embedding -> embedding,
				(left, right) -> left
			));
	}

	private Map<Long, PromotionProductSnapshot> findActivePromotionProductMap(Collection<Long> productIds) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return promotionProductRepository.findActivePromotionProductsByProductIds(
				productIds,
				PromotionStatus.ENDED,
				LocalDateTime.now()
			)
			.stream()
			.collect(Collectors.toMap(
				PromotionProductSnapshot::getProductId,
				promotionProduct -> promotionProduct,
				this::selectBetterPromotionProduct
			));
	}

	private PromotionProductSnapshot selectBetterPromotionProduct(
		PromotionProductSnapshot left,
		PromotionProductSnapshot right
	) {
		Integer leftPrice = left.getPromotionPrice();
		Integer rightPrice = right.getPromotionPrice();

		if (leftPrice == null && rightPrice == null) {
			return left;
		}

		if (leftPrice == null) {
			return right;
		}

		if (rightPrice == null) {
			return left;
		}

		return leftPrice <= rightPrice ? left : right;
	}

	private double calculateScore(
		ProductSnapshot sourceProduct,
		PromotionProductSnapshot sourcePromotionProduct,
		ProductEmbedding sourceEmbedding,
		ProductSnapshot candidate,
		PromotionProductSnapshot candidatePromotionProduct,
		ProductEmbedding candidateEmbedding,
		InventorySnapshot candidateInventory
	) {
		double score = 0.25;

		if (candidate.getCategoryId().equals(sourceProduct.getCategoryId())) {
			score += 0.20;
		}

		if (sourceEmbedding != null && candidateEmbedding != null) {
			double similarity = VectorSimilarityCalculator.cosineSimilarity(
				sourceEmbedding.getEmbeddingVector(),
				candidateEmbedding.getEmbeddingVector()
			);

			score += normalizeSimilarity(similarity) * 0.30;
		}

		score += calculatePriceSimilarityScore(
			sourceProduct,
			sourcePromotionProduct,
			candidate,
			candidatePromotionProduct
		) * 0.15;

		score += calculateStockScore(candidateInventory) * 0.05;

		if (isSameBrand(sourceProduct, candidate)) {
			score += 0.03;
		}

		return clamp(score);
	}

	private double calculatePriceSimilarityScore(
		ProductSnapshot sourceProduct,
		PromotionProductSnapshot sourcePromotionProduct,
		ProductSnapshot candidate,
		PromotionProductSnapshot candidatePromotionProduct
	) {
		Integer sourcePrice = resolveSalePrice(sourceProduct, sourcePromotionProduct);
		Integer candidatePrice = resolveSalePrice(candidate, candidatePromotionProduct);

		if (sourcePrice == null || sourcePrice <= 0 || candidatePrice == null || candidatePrice <= 0) {
			return 0.0;
		}

		double diffRate = Math.abs(sourcePrice - candidatePrice) / (double)sourcePrice;

		if (diffRate <= 0.10) {
			return 1.0;
		}

		if (diffRate <= 0.30) {
			return 0.7;
		}

		if (diffRate <= 0.50) {
			return 0.4;
		}

		return 0.1;
	}

	private Integer resolveSalePrice(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct
	) {
		if (product == null || product.getOriginalPrice() == null) {
			return null;
		}

		Integer originalPrice = product.getOriginalPrice();

		if (promotionProduct == null || promotionProduct.getPromotionPrice() == null) {
			return originalPrice;
		}

		Integer promotionPrice = promotionProduct.getPromotionPrice();

		if (promotionPrice <= 0 || promotionPrice >= originalPrice) {
			return originalPrice;
		}

		return promotionPrice;
	}

	private double calculateStockScore(InventorySnapshot inventory) {
		if (inventory == null || !inventory.hasAvailableStock()) {
			return 0.0;
		}

		if (inventory.isNormalStock()) {
			return 1.0;
		}

		if (inventory.isLowStock()) {
			return 0.4;
		}

		return 0.7;
	}

	private String createReason(
		InventorySnapshot sourceInventory,
		ProductSnapshot candidate,
		boolean force
	) {
		if (force && !sourceInventory.needsSubstituteRecommendation()) {
			return "쇼핑리스트에서 선택한 재료를 대신 담을 수 있는 같은 카테고리 상품입니다.";
		}

		if (sourceInventory.isOutOfStock()) {
			return "품절된 상품과 같은 카테고리의 구매 가능한 대체 상품입니다.";
		}

		if (sourceInventory.isLowStock()) {
			return "재고가 부족한 상품과 유사한 구매 가능한 대체 상품입니다.";
		}

		return "재고가 얼마 남지 않은 상품 대신 구매할 수 있는 대체 상품입니다.";
	}

	private SubstituteRecommendationResponse emptyResponse(
		Long userId,
		Long storeId,
		ProductSnapshot sourceProduct,
		String sourceCategoryName,
		InventorySnapshot sourceInventory
	) {
		return new SubstituteRecommendationResponse(
			userId,
			storeId,
			sourceProduct.getProductId(),
			SourceProductResponse.from(sourceProduct, sourceCategoryName),
			sourceInventory == null ? null : SourceInventoryResponse.from(sourceInventory),
			RecommendationType.SUBSTITUTE,
			List.of()
		);
	}

	private boolean isSameBrand(
		ProductSnapshot sourceProduct,
		ProductSnapshot candidate
	) {
		String sourceBrand = sourceProduct.getBrandName();
		String candidateBrand = candidate.getBrandName();

		if (sourceBrand == null || sourceBrand.isBlank()) {
			return false;
		}

		return sourceBrand.equals(candidateBrand);
	}

	private double normalizeSimilarity(double similarity) {
		if (similarity <= 0) {
			return 0.0;
		}

		return Math.min(similarity, 1.0);
	}

	private double clamp(double score) {
		if (score < 0.0) {
			return 0.0;
		}

		return Math.min(score, 1.0);
	}

	private int normalizeSize(int size) {
		return requestValidator.normalizeSize(size, DEFAULT_SIZE, MAX_SIZE);
	}
}