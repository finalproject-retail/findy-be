package com.princesses7.findy.shopping.product.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRankingService {

	private static final String PRODUCT_VIEW_KEY_PREFIX = "ranking:product-view:daily:";
	private static final String PRODUCT_CART_KEY_PREFIX = "ranking:product-cart:daily:";
	private static final String PRODUCT_PURCHASE_KEY_PREFIX = "ranking:product-purchase:daily:";
	private static final String PRODUCT_RECOMMENDATION_CLICK_KEY_PREFIX = "ranking:product-recommendation-click:daily:";
	private static final String FINDY_MART_RECOMMEND_KEY_PREFIX = "ranking:findy-mart-recommend:daily:";

	private static final int LOOKBACK_DAYS = 7;
	private static final int RETENTION_DAYS = 14;

	private static final double VIEW_WEIGHT = 1.0;
	private static final double CART_WEIGHT = 3.0;
	private static final double PURCHASE_WEIGHT = 8.0;
	private static final double RECOMMENDATION_CLICK_WEIGHT = 2.0;
	private static final double FINDY_MART_RECOMMEND_WEIGHT = 1.0;

	private final StringRedisTemplate redisTemplate;

	public void recordView(Long productId) {
		recordDailyCount(PRODUCT_VIEW_KEY_PREFIX, productId, 1);
	}

	public void recordCartAdded(Long productId) {
		recordCartAdded(productId, 1);
	}

	public void recordCartAdded(Long productId, Integer quantity) {
		recordDailyCount(PRODUCT_CART_KEY_PREFIX, productId, resolveQuantity(quantity));
	}

	public void recordPurchase(Long productId) {
		recordPurchase(productId, 1);
	}

	public void recordPurchase(Long productId, Integer quantity) {
		recordDailyCount(PRODUCT_PURCHASE_KEY_PREFIX, productId, resolveQuantity(quantity));
	}

	public void recordRecommendationClick(Long productId) {
		recordDailyCount(PRODUCT_RECOMMENDATION_CLICK_KEY_PREFIX, productId, 1);
	}

	public void recordFindyMartRecommended(Long productId) {
		recordDailyCount(FINDY_MART_RECOMMEND_KEY_PREFIX, productId, 1);
	}

	public List<Long> getAllPopularProductIds() {
		return getWeightedRankedProductIds(
			List.of(
				RankingSource.of(PRODUCT_VIEW_KEY_PREFIX, VIEW_WEIGHT),
				RankingSource.of(PRODUCT_CART_KEY_PREFIX, CART_WEIGHT),
				RankingSource.of(PRODUCT_PURCHASE_KEY_PREFIX, PURCHASE_WEIGHT),
				RankingSource.of(PRODUCT_RECOMMENDATION_CLICK_KEY_PREFIX, RECOMMENDATION_CLICK_WEIGHT)
			),
			null
		);
	}

	public List<Long> getPopularProductIds(int limit) {
		if (limit < 1) {
			return List.of();
		}

		return getWeightedRankedProductIds(
			List.of(
				RankingSource.of(PRODUCT_VIEW_KEY_PREFIX, VIEW_WEIGHT),
				RankingSource.of(PRODUCT_CART_KEY_PREFIX, CART_WEIGHT),
				RankingSource.of(PRODUCT_PURCHASE_KEY_PREFIX, PURCHASE_WEIGHT),
				RankingSource.of(PRODUCT_RECOMMENDATION_CLICK_KEY_PREFIX, RECOMMENDATION_CLICK_WEIGHT)
			),
			limit
		);
	}

	public List<Long> getFindyMartRecommendedProductIds(int limit) {
		if (limit < 1) {
			return List.of();
		}

		return getWeightedRankedProductIds(
			List.of(
				RankingSource.of(FINDY_MART_RECOMMEND_KEY_PREFIX, FINDY_MART_RECOMMEND_WEIGHT)
			),
			limit
		);
	}

	private void recordDailyCount(
		String keyPrefix,
		Long productId,
		int score
	) {
		if (productId == null || score < 1) {
			return;
		}

		try {
			String key = createKey(keyPrefix, LocalDate.now());
			String value = String.valueOf(productId);

			redisTemplate.opsForZSet().incrementScore(key, value, score);
			redisTemplate.expire(key, Duration.ofDays(RETENTION_DAYS));
		} catch (RuntimeException exception) {
			log.warn(
				"상품 랭킹 기록에 실패했습니다. keyPrefix={}, productId={}, score={}",
				keyPrefix,
				productId,
				score,
				exception
			);
		}
	}

	private List<Long> getWeightedRankedProductIds(
		List<RankingSource> rankingSources,
		Integer limit
	) {
		try {
			Map<Long, Double> scoreMap = new HashMap<>();

			for (RankingSource rankingSource : rankingSources) {
				mergeRankingScores(scoreMap, rankingSource);
			}

			if (scoreMap.isEmpty()) {
				return List.of();
			}

			var stream = scoreMap.entrySet().stream()
				.sorted(
					Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder())
						.thenComparing(Map.Entry.comparingByKey())
				);

			if (limit != null) {
				stream = stream.limit(limit);
			}

			return stream
				.map(Map.Entry::getKey)
				.toList();
		} catch (RuntimeException exception) {
			log.warn("Redis 상품 랭킹 조회에 실패했습니다.", exception);
			return List.of();
		}
	}

	private void mergeRankingScores(
		Map<Long, Double> scoreMap,
		RankingSource rankingSource
	) {
		for (int i = 0; i < LOOKBACK_DAYS; i++) {
			String key = createKey(
				rankingSource.keyPrefix(),
				LocalDate.now().minusDays(i)
			);

			Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
				.reverseRangeWithScores(key, 0, -1);

			if (tuples == null || tuples.isEmpty()) {
				continue;
			}

			for (ZSetOperations.TypedTuple<String> tuple : tuples) {
				Optional<Long> productId = parseProductId(tuple.getValue());
				Double score = tuple.getScore();

				if (productId.isEmpty() || score == null) {
					continue;
				}

				double weightedScore = score * rankingSource.weight();
				scoreMap.merge(productId.get(), weightedScore, Double::sum);
			}
		}
	}

	private int resolveQuantity(Integer quantity) {
		if (quantity == null || quantity < 1) {
			return 1;
		}

		return quantity;
	}

	private String createKey(String keyPrefix, LocalDate date) {
		return keyPrefix + date;
	}

	private Optional<Long> parseProductId(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}

		try {
			return Optional.of(Long.valueOf(value));
		} catch (NumberFormatException exception) {
			return Optional.empty();
		}
	}

	private record RankingSource(
		String keyPrefix,
		double weight
	) {

		private static RankingSource of(String keyPrefix, double weight) {
			return new RankingSource(keyPrefix, weight);
		}
	}
}