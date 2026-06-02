package com.retail.apigateway.auth;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GatewayJwtValidator {

	private static final String BLACKLIST_KEY_PREFIX = "user-service:jwt:bl:";

	private final Key signingKey;
	private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

	public GatewayJwtValidator(
		GatewayJwtProperties jwtProperties,
		ObjectProvider<StringRedisTemplate> redisTemplateProvider
	) {
		this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
		this.redisTemplateProvider = redisTemplateProvider;
	}

	public Long resolveUserId(String authorizationHeader) {
		String token = extractBearerToken(authorizationHeader);
		if (!StringUtils.hasText(token)) {
			return null;
		}

		try {
			Claims claims = Jwts.parserBuilder()
				.setSigningKey(signingKey)
				.build()
				.parseClaimsJws(token)
				.getBody();

			String jti = claims.getId();
			if (jti != null && isBlacklisted(jti)) {
				return null;
			}

			return Long.parseLong(claims.getSubject());
		} catch (JwtException | IllegalArgumentException exception) {
			log.debug(
				"JWT validation failed: type={}, message={}",
				exception.getClass().getSimpleName(),
				exception.getMessage()
			);
			return null;
		}
	}

	private boolean isBlacklisted(String jti) {
		StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
		if (redisTemplate == null) {
			return false;
		}

		try {
			return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + jti));
		} catch (Exception exception) {
			log.warn("Redis blacklist check failed. jti={}", jti, exception);
			return false;
		}
	}

	private String extractBearerToken(String authorizationHeader) {
		if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(GatewayAuthHeaders.BEARER_PREFIX)) {
			return null;
		}

		return authorizationHeader.substring(GatewayAuthHeaders.BEARER_PREFIX.length()).trim();
	}
}
