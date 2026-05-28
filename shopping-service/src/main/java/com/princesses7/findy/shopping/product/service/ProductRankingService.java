package com.princesses7.findy.shopping.product.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
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

	private static final String KEY_PREFIX = "ranking:product-view:daily:";
	private static final int EXPIRE_DAYS = 2;

	private final StringRedisTemplate redisTemplate;

	public void recordView(Long productId) {
		if (productId == null) {
			return;
		}

		try {
			String key = createTodayKey();
			String value = String.valueOf(productId);

			redisTemplate.opsForZSet().incrementScore(key, value, 1);
			redisTemplate.expire(key, Duration.ofDays(EXPIRE_DAYS));
		} catch (RuntimeException exception) {
			log.warn("상품 조회 랭킹 기록에 실패했습니다. productId={}", productId, exception);
		}
	}

	public List<Long> getPopularProductIds(int limit) {
		if (limit < 1) {
			return List.of();
		}

		try {
			Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
				.reverseRangeWithScores(createTodayKey(), 0, limit - 1);

			if (tuples == null || tuples.isEmpty()) {
				return List.of();
			}

			return tuples.stream()
				.map(ZSetOperations.TypedTuple::getValue)
				.map(this::parseProductId)
				.flatMap(Optional::stream)
				.toList();
		} catch (RuntimeException exception) {
			log.warn("인기 상품 랭킹 조회에 실패했습니다.", exception);
			return List.of();
		}
	}

	private String createTodayKey() {
		return KEY_PREFIX + LocalDate.now();
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