package com.princesses7.findy.shopping.search.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import com.princesses7.findy.shopping.search.dto.response.TrendingSearchKeywordResponse;

@ExtendWith(MockitoExtension.class)
class SearchKeywordRankingServiceTest {

	private static final String KEY_PREFIX = "ranking:search:daily:";
	private static final String TODAY_KEY = KEY_PREFIX + LocalDate.now();

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ZSetOperations<String, String> zSetOperations;

	private SearchKeywordRankingService searchKeywordRankingService;

	@BeforeEach
	void setUp() {
		searchKeywordRankingService = new SearchKeywordRankingService(redisTemplate);
	}

	@Test
	@DisplayName("검색어를 Redis Sorted Set에 기록하고 14일 동안 보관한다")
	void recordSearchKeyword() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		searchKeywordRankingService.record(" 우유 ");

		verify(zSetOperations).incrementScore(TODAY_KEY, "우유", 1.0);
		verify(redisTemplate).expire(TODAY_KEY, Duration.ofDays(14));
	}

	@Test
	@DisplayName("검색어 내부 공백을 정규화하여 기록한다")
	void recordNormalizedSearchKeyword() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		searchKeywordRankingService.record(" 비건   간식 ");

		verify(zSetOperations).incrementScore(TODAY_KEY, "비건 간식", 1.0);
		verify(redisTemplate).expire(TODAY_KEY, Duration.ofDays(14));
	}

	@Test
	@DisplayName("검색어가 null이면 Redis에 기록하지 않는다")
	void doNotRecordNullKeyword() {
		searchKeywordRankingService.record(null);

		verifyNoInteractions(redisTemplate);
	}

	@Test
	@DisplayName("검색어가 공백이면 Redis에 기록하지 않는다")
	void doNotRecordBlankKeyword() {
		searchKeywordRankingService.record(" ");

		verifyNoInteractions(redisTemplate);
	}

	@Test
	@DisplayName("최근 7일 인기 검색어를 합산하여 점수 내림차순으로 조회한다")
	void getTrendingKeywordsForLast7Days() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		String todayKey = createKey(LocalDate.now());
		String yesterdayKey = createKey(LocalDate.now().minusDays(1));
		String twoDaysAgoKey = createKey(LocalDate.now().minusDays(2));

		when(zSetOperations.reverseRangeWithScores(todayKey, 0, -1))
			.thenReturn(tupleSet(
				createTuple("우유", 3.0),
				createTuple("라면", 2.0)
			));

		when(zSetOperations.reverseRangeWithScores(yesterdayKey, 0, -1))
			.thenReturn(tupleSet(
				createTuple("우유", 2.0),
				createTuple("생수", 4.0)
			));

		when(zSetOperations.reverseRangeWithScores(twoDaysAgoKey, 0, -1))
			.thenReturn(tupleSet(
				createTuple("라면", 5.0)
			));

		when(zSetOperations.reverseRangeWithScores(
			argThat(key -> !key.equals(todayKey)
				&& !key.equals(yesterdayKey)
				&& !key.equals(twoDaysAgoKey)),
			eq(0L),
			eq(-1L)
		)).thenReturn(Set.of());

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(10);

		assertThat(responses).hasSize(3);

		assertThat(responses.get(0).rank()).isEqualTo(1);
		assertThat(responses.get(0).keyword()).isEqualTo("라면");
		assertThat(responses.get(0).score()).isEqualTo(7);

		assertThat(responses.get(1).rank()).isEqualTo(2);
		assertThat(responses.get(1).keyword()).isEqualTo("우유");
		assertThat(responses.get(1).score()).isEqualTo(5);

		assertThat(responses.get(2).rank()).isEqualTo(3);
		assertThat(responses.get(2).keyword()).isEqualTo("생수");
		assertThat(responses.get(2).score()).isEqualTo(4);

		verify(zSetOperations, times(7))
			.reverseRangeWithScores(anyString(), eq(0L), eq(-1L));
	}

	@Test
	@DisplayName("limit이 null이면 기본 10개까지만 반환한다")
	void getTrendingKeywordsWithDefaultLimit() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
			.thenReturn(tupleSet(
				createTuple("키워드1", 20.0),
				createTuple("키워드2", 19.0),
				createTuple("키워드3", 18.0),
				createTuple("키워드4", 17.0),
				createTuple("키워드5", 16.0),
				createTuple("키워드6", 15.0),
				createTuple("키워드7", 14.0),
				createTuple("키워드8", 13.0),
				createTuple("키워드9", 12.0),
				createTuple("키워드10", 11.0),
				createTuple("키워드11", 10.0)
			));

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(null);

		assertThat(responses).hasSize(10);
	}

	@Test
	@DisplayName("limit이 1보다 작으면 기본 10개까지만 반환한다")
	void getTrendingKeywordsWithInvalidLimit() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
			.thenReturn(tupleSet(
				createTuple("키워드1", 20.0),
				createTuple("키워드2", 19.0),
				createTuple("키워드3", 18.0),
				createTuple("키워드4", 17.0),
				createTuple("키워드5", 16.0),
				createTuple("키워드6", 15.0),
				createTuple("키워드7", 14.0),
				createTuple("키워드8", 13.0),
				createTuple("키워드9", 12.0),
				createTuple("키워드10", 11.0),
				createTuple("키워드11", 10.0)
			));

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(0);

		assertThat(responses).hasSize(10);
	}

	@Test
	@DisplayName("limit이 최대값보다 크면 20개까지만 반환한다")
	void getTrendingKeywordsWithMaxLimit() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		LinkedHashSet<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();

		for (int i = 1; i <= 25; i++) {
			tuples.add(createTuple("키워드" + i, 100.0 - i));
		}

		when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
			.thenReturn(tuples);

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(100);

		assertThat(responses).hasSize(20);
	}

	@Test
	@DisplayName("Redis 조회 결과가 없으면 빈 목록을 반환한다")
	void returnEmptyListWhenRedisResultIsEmpty() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
			.thenReturn(Set.of());

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(10);

		assertThat(responses).isEmpty();
	}

	@Test
	@DisplayName("Redis 기록 실패가 발생해도 예외를 전파하지 않는다")
	void doNotThrowExceptionWhenRecordFails() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble()))
			.thenThrow(new RuntimeException("Redis connection failed"));

		assertThatCode(() -> searchKeywordRankingService.record("우유"))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Redis 조회 실패가 발생하면 빈 목록을 반환한다")
	void returnEmptyListWhenGetTrendingKeywordsFails() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
			.thenThrow(new RuntimeException("Redis connection failed"));

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(10);

		assertThat(responses).isEmpty();
	}

	private String createKey(LocalDate date) {
		return KEY_PREFIX + date;
	}

	@SafeVarargs
	private Set<ZSetOperations.TypedTuple<String>> tupleSet(
		ZSetOperations.TypedTuple<String>... tuples
	) {
		return new LinkedHashSet<>(List.of(tuples));
	}

	private ZSetOperations.TypedTuple<String> createTuple(
		String keyword,
		Double score
	) {
		return new DefaultTypedTuple<>(keyword, score);
	}
}