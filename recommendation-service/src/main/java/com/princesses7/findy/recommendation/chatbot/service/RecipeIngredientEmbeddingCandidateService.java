package com.princesses7.findy.recommendation.chatbot.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;
import com.princesses7.findy.recommendation.chatbot.dto.RecipeIngredientItem;
import com.princesses7.findy.recommendation.chatbot.repository.ShoppingProductReadRepository;
import com.princesses7.findy.recommendation.embedding.entity.ProductEmbedding;
import com.princesses7.findy.recommendation.embedding.repository.ProductEmbeddingRepository;
import com.princesses7.findy.recommendation.embedding.util.VectorSimilarityCalculator;
import com.princesses7.findy.recommendation.external.embedding.ProductEmbeddingClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeIngredientEmbeddingCandidateService {

	private static final int EMBEDDING_PREFETCH_LIMIT = 120;
	private static final double MIN_EMBEDDING_SIMILARITY = 0.10;

	private final ProductEmbeddingClient productEmbeddingClient;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final ShoppingProductReadRepository shoppingProductReadRepository;

	public List<ChatbotShoppingProduct> findCandidates(
		String recipeName,
		RecipeIngredientItem ingredient,
		Long storeId,
		int limit
	) {
		if (ingredient == null || !hasText(ingredient.ingredientName())) {
			return List.of();
		}

		try {
			List<Double> ingredientEmbedding = productEmbeddingClient.createEmbedding(
				createIngredientEmbeddingText(recipeName, ingredient)
			);

			if (ingredientEmbedding.isEmpty()) {
				return List.of();
			}

			List<ScoredProductEmbedding> scoredEmbeddings = findSimilarProductEmbeddings(
				ingredientEmbedding,
				Math.max(limit * 4, EMBEDDING_PREFETCH_LIMIT)
			);

			if (scoredEmbeddings.isEmpty()) {
				return List.of();
			}

			List<Long> productIds = scoredEmbeddings.stream()
				.map(ScoredProductEmbedding::productId)
				.toList();

			List<ChatbotShoppingProduct> products = shoppingProductReadRepository.findByProductIds(
				productIds,
				storeId,
				productIds.size()
			);

			Map<Long, ChatbotShoppingProduct> productMap = products.stream()
				.filter(product -> product != null && product.productId() != null)
				.collect(Collectors.toMap(
					ChatbotShoppingProduct::productId,
					product -> product,
					(left, right) -> left,
					LinkedHashMap::new
				));

			return scoredEmbeddings.stream()
				.map(scored -> productMap.get(scored.productId()))
				.filter(product -> product != null && product.isRecommendable())
				.filter(this::hasStock)
				.limit(limit)
				.toList();
		} catch (Exception exception) {
			log.warn(
				"Recipe ingredient embedding candidate search failed. recipeName={}, ingredientName={}, message={}",
				recipeName,
				ingredient.ingredientName(),
				exception.getMessage()
			);

			return List.of();
		}
	}

	private List<ScoredProductEmbedding> findSimilarProductEmbeddings(
		List<Double> ingredientEmbedding,
		int limit
	) {
		List<ProductEmbedding> productEmbeddings = productEmbeddingRepository.findByModelAndDimensions(
			productEmbeddingClient.model(),
			productEmbeddingClient.dimensions()
		);

		if (productEmbeddings.isEmpty()) {
			return List.of();
		}

		return productEmbeddings.stream()
			.map(productEmbedding -> new ScoredProductEmbedding(
				productEmbedding.getProductId(),
				VectorSimilarityCalculator.cosineSimilarity(
					ingredientEmbedding,
					productEmbedding.getEmbeddingVector()
				)
			))
			.filter(scored -> scored.similarity() >= MIN_EMBEDDING_SIMILARITY)
			.sorted(Comparator.comparing(ScoredProductEmbedding::similarity).reversed())
			.limit(limit)
			.toList();
	}

	private String createIngredientEmbeddingText(
		String recipeName,
		RecipeIngredientItem ingredient
	) {
		return """
			대형마트 레시피 재료 상품 검색 요청입니다.
			
			요리명: %s
			찾는 재료명: %s
			필요 수량: %s
			
			검색 목표:
			이 요리를 직접 만들기 위해 필요한 재료 자체 상품을 찾습니다.
			원재료, 신선식품, 손질 재료, 조리용 기본 재료를 우선합니다.
			
			제외 대상:
			완제품, 즉석식품, 레토르트, 밀키트, 도시락 등의 상품은 제외합니다.
			
			예시:
			김치찌개 재료 김치에는 포기김치, 배추김치, 맛김치가 적합합니다.
			김치찌개 완제품, 레토르트 김치찌개, 돼지고기김치찌개 완제품은 부적합합니다.
			""".formatted(
			nullToEmpty(recipeName),
			nullToEmpty(ingredient.ingredientName()),
			nullToEmpty(ingredient.quantityText())
		);
	}

	private boolean hasStock(ChatbotShoppingProduct product) {
		return product.stockQuantity() != null && product.stockQuantity() > 0;
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private record ScoredProductEmbedding(
		Long productId,
		double similarity
	) {
	}
}