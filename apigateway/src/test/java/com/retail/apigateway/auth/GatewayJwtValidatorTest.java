package com.retail.apigateway.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class GatewayJwtValidatorTest {

	private static final String SECRET = "test-jwt-secret-key-must-be-long-enough-for-hs256-signing";

	private GatewayJwtValidator validator;

	@BeforeEach
	void setUp() {
		GatewayJwtProperties properties = new GatewayJwtProperties(SECRET);
		ObjectProvider<StringRedisTemplate> redisProvider = new ObjectProvider<>() {
			@Override
			public StringRedisTemplate getObject() {
				return null;
			}

			@Override
			public StringRedisTemplate getObject(Object... args) {
				return null;
			}

			@Override
			public StringRedisTemplate getIfAvailable() {
				return null;
			}

			@Override
			public StringRedisTemplate getIfUnique() {
				return null;
			}
		};
		validator = new GatewayJwtValidator(properties, redisProvider);
	}

	@Test
	void resolvesUserIdFromValidToken() {
		String token = Jwts.builder()
			.setSubject("42")
			.setId("jti-1")
			.setExpiration(new Date(System.currentTimeMillis() + 60_000))
			.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
			.compact();

		Long userId = validator.resolveUserId("Bearer " + token);

		assertThat(userId).isEqualTo(42L);
	}

	@Test
	void rejectsMissingBearerToken() {
		assertThat(validator.resolveUserId(null)).isNull();
		assertThat(validator.resolveUserId("invalid")).isNull();
	}
}
