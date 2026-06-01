package com.princesses7.findy.shopping.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@TestConfiguration
public class TestRedisConfig {

	@Bean
	@Primary
	StringRedisTemplate stringRedisTemplate() {
		StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.incrementScore(anyString(), anyString(), any(Double.class))).thenReturn(1.0);
		return redisTemplate;
	}
}
