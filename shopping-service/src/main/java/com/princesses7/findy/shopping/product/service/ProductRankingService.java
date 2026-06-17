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
	private static final String FINDY_MART_RECOMMEND_KEY_PREFIX = "ranking:findy-mart-recommend:daily:";

	private static final int LOOKBACK_DAYS = 7;
	private static final int RETENTION_DAYS = 14;

	private final StringRedisTemplate redisTemplate;

	public void recordView(Long productId) {
		if (productId == null) {
			return;
		}

		try {
			String key = createKey(PRODUCT_VIEW_KEY_PREFIX, LocalDate.now());
			String value = String.valueOf(productId);

			redisTemplate.opsForZSet().incrementScore(key, value, 1);
			redisTemplate.expire(key, Duration.ofDays(RETENTION_DAYS));
		} catch (RuntimeException exception) {
			log.warn("상품 조회 랭킹 기록에 실패했습니다. productId={}", productId, exception);
		}
	}

	public List<Long> getAllPopularProductIds() {
		return getRankedProductIds(PRODUCT_VIEW_KEY_PREFIX, null);
	}

	public List<Long> getPopularProductIds(int limit) {
		if (limit < 1) {
			return List.of();
		}

		return getRankedProductIds(PRODUCT_VIEW_KEY_PREFIX, limit);
	}

	public List<Long> getFindyMartRecommendedProductIds(int limit) {
		if (limit < 1) {
			return List.of();
		}

		return getRankedProductIds(FINDY_MART_RECOMMEND_KEY_PREFIX, limit);
	}

	private List<Long> getRankedProductIds(String keyPrefix, Integer limit) {
		try {
			Map<String, Double> scoreMap = new HashMap<>();

			for (int i = 0; i < LOOKBACK_DAYS; i++) {
				String key = createKey(keyPrefix, LocalDate.now().minusDays(i));

				Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
					.reverseRangeWithScores(key, 0, -1);

				if (tuples == null || tuples.isEmpty()) {
					continue;
				}

				for (ZSetOperations.TypedTuple<String> tuple : tuples) {
					String productId = tuple.getValue();
					Double score = tuple.getScore();

					if (productId == null || score == null) {
						continue;
					}

					scoreMap.merge(productId, score, Double::sum);
				}
			}

			if (scoreMap.isEmpty()) {
				return List.of();
			}

			var stream = scoreMap.entrySet().stream()
				.sorted(
					Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder())
						.thenComparing(Map.Entry.comparingByKey())
				);

			if (limit != null) {
				stream = stream.limit(limit);
			}

			return stream
				.map(Map.Entry::getKey)
				.map(this::parseProductId)
				.flatMap(Optional::stream)
				.toList();
		} catch (RuntimeException exception) {
			log.warn("Redis 상품 랭킹 조회에 실패했습니다. keyPrefix={}", keyPrefix, exception);
			return List.of();
		}
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
}