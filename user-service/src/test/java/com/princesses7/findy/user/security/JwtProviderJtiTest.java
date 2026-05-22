package com.princesses7.findy.user.security;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

class JwtProviderJtiTest {

	private static final String SECRET = "princess7findysecretsuperkey12345678";

	@Test
	void createToken_includesJtiClaim() {
		JwtProvider jwtProvider = new JwtProvider(SECRET, 3_600_000L, noopBlacklist());

		String token = jwtProvider.createToken("1", "user@example.com", "USER");

		Claims claims = Jwts.parserBuilder()
			.setSigningKey(SECRET.getBytes())
			.build()
			.parseClaimsJws(token)
			.getBody();

		assertThat(claims.getId()).isNotBlank();
	}

	private static TokenBlacklist noopBlacklist() {
		return new TokenBlacklist() {
			@Override
			public boolean isBlacklisted(String jti) {
				return false;
			}

			@Override
			public void blacklistUntil(String jti, Instant expiry) {
			}
		};
	}
}
