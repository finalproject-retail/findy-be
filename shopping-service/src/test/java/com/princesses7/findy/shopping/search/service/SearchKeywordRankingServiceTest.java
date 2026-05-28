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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import com.princesses7.findy.shopping.search.dto.response.TrendingSearchKeywordResponse;

@ExtendWith(MockitoExtension.class)
class SearchKeywordRankingServiceTest {

	private static final String TODAY_KEY = "ranking:search:daily:" + LocalDate.now();

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
	@DisplayName("검색어를 Redis Sorted Set에 기록한다")
	void recordSearchKeyword() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		searchKeywordRankingService.record(" 우유 ");

		verify(zSetOperations).incrementScore(TODAY_KEY, "우유", 1.0);
		verify(redisTemplate).expire(TODAY_KEY, Duration.ofDays(2));
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
	@DisplayName("실시간 인기 검색어를 점수 내림차순으로 조회한다")
	void getTrendingKeywords() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
		tuples.add(createTuple("우유", 3.0));
		tuples.add(createTuple("라면", 2.0));
		tuples.add(createTuple("생수", 1.0));

		when(zSetOperations.reverseRangeWithScores(TODAY_KEY, 0, 9))
			.thenReturn(tuples);

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(10);

		assertThat(responses).hasSize(3);

		assertThat(responses.get(0).rank()).isEqualTo(1);
		assertThat(responses.get(0).keyword()).isEqualTo("우유");
		assertThat(responses.get(0).score()).isEqualTo(3);

		assertThat(responses.get(1).rank()).isEqualTo(2);
		assertThat(responses.get(1).keyword()).isEqualTo("라면");
		assertThat(responses.get(1).score()).isEqualTo(2);

		assertThat(responses.get(2).rank()).isEqualTo(3);
		assertThat(responses.get(2).keyword()).isEqualTo("생수");
		assertThat(responses.get(2).score()).isEqualTo(1);
	}

	@Test
	@DisplayName("limit이 null이면 기본 10개를 조회한다")
	void getTrendingKeywordsWithDefaultLimit() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.reverseRangeWithScores(TODAY_KEY, 0, 9))
			.thenReturn(Set.of());

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(null);

		assertThat(responses).isEmpty();

		verify(zSetOperations).reverseRangeWithScores(TODAY_KEY, 0, 9);
	}

	@Test
	@DisplayName("limit이 1보다 작으면 기본 10개를 조회한다")
	void getTrendingKeywordsWithInvalidLimit() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.reverseRangeWithScores(TODAY_KEY, 0, 9))
			.thenReturn(Set.of());

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(0);

		assertThat(responses).isEmpty();

		verify(zSetOperations).reverseRangeWithScores(TODAY_KEY, 0, 9);
	}

	@Test
	@DisplayName("limit이 최대값보다 크면 20개까지만 조회한다")
	void getTrendingKeywordsWithMaxLimit() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.reverseRangeWithScores(TODAY_KEY, 0, 19))
			.thenReturn(Set.of());

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(100);

		assertThat(responses).isEmpty();

		verify(zSetOperations).reverseRangeWithScores(TODAY_KEY, 0, 19);
	}

	@Test
	@DisplayName("Redis 조회 결과가 없으면 빈 목록을 반환한다")
	void returnEmptyListWhenRedisResultIsEmpty() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.reverseRangeWithScores(TODAY_KEY, 0, 9))
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
		when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
			.thenThrow(new RuntimeException("Redis connection failed"));

		List<TrendingSearchKeywordResponse> responses =
			searchKeywordRankingService.getTrendingKeywords(10);

		assertThat(responses).isEmpty();
	}

	@SuppressWarnings("unchecked")
	private ZSetOperations.TypedTuple<String> createTuple(
		String keyword,
		Double score
	) {
		ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);

		when(tuple.getValue()).thenReturn(keyword);
		when(tuple.getScore()).thenReturn(score);

		return tuple;
	}
}