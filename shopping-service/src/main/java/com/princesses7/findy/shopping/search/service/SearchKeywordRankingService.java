package com.princesses7.findy.shopping.search.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.princesses7.findy.shopping.search.dto.response.TrendingSearchKeywordResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchKeywordRankingService {

	private static final String KEY_PREFIX = "ranking:search:daily:";
	private static final int DEFAULT_LIMIT = 10;
	private static final int MAX_LIMIT = 20;

	private static final int LOOKBACK_DAYS = 7;

	private static final int RETENTION_DAYS = 14;

	private final StringRedisTemplate redisTemplate;

	public void record(String keyword) {
		String normalizedKeyword = normalizeKeyword(keyword);

		if (normalizedKeyword == null) {
			return;
		}

		try {
			String key = createKey(LocalDate.now());

			redisTemplate.opsForZSet().incrementScore(key, normalizedKeyword, 1);
			redisTemplate.expire(key, Duration.ofDays(RETENTION_DAYS));
		} catch (RuntimeException exception) {
			log.warn("검색어 랭킹 기록에 실패했습니다. keyword={}", normalizedKeyword, exception);
		}
	}

	public List<TrendingSearchKeywordResponse> getTrendingKeywords(Integer limit) {
		int resolvedLimit = resolveLimit(limit);

		try {
			Map<String, Double> scoreMap = new HashMap<>();

			for (int i = 0; i < LOOKBACK_DAYS; i++) {
				String key = createKey(LocalDate.now().minusDays(i));

				Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
					.reverseRangeWithScores(key, 0, -1);

				if (tuples == null || tuples.isEmpty()) {
					continue;
				}

				for (ZSetOperations.TypedTuple<String> tuple : tuples) {
					String keyword = tuple.getValue();
					Double score = tuple.getScore();

					if (keyword == null || score == null) {
						continue;
					}

					scoreMap.merge(keyword, score, Double::sum);
				}
			}

			if (scoreMap.isEmpty()) {
				return List.of();
			}

			AtomicInteger rank = new AtomicInteger(1);

			return scoreMap.entrySet().stream()
				.sorted(
					Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder())
						.thenComparing(Map.Entry.comparingByKey())
				)
				.limit(resolvedLimit)
				.map(entry -> new TrendingSearchKeywordResponse(
					rank.getAndIncrement(),
					entry.getKey(),
					entry.getValue().longValue()
				))
				.toList();
		} catch (RuntimeException exception) {
			log.warn("최근 7일 검색어 랭킹 조회에 실패했습니다.", exception);
			return List.of();
		}
	}

	private String createKey(LocalDate date) {
		return KEY_PREFIX + date;
	}

	private int resolveLimit(Integer limit) {
		if (limit == null || limit < 1) {
			return DEFAULT_LIMIT;
		}

		return Math.min(limit, MAX_LIMIT);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}

		return keyword.trim().replaceAll("\\s+", " ");
	}
}