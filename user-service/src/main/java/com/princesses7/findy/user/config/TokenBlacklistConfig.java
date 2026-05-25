package com.princesses7.findy.user.config;

import java.time.Instant;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.princesses7.findy.user.security.RedisTokenBlacklist;
import com.princesses7.findy.user.security.TokenBlacklist;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class TokenBlacklistConfig {

	@Bean
	TokenBlacklist tokenBlacklist(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
		StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
		if (redisTemplate != null) {
			log.info("Token blacklist enabled with Redis.");
			return new RedisTokenBlacklist(redisTemplate);
		}
		log.warn("StringRedisTemplate not available; token blacklist is disabled (Noop).");
		return noopTokenBlacklist();
	}

	private static TokenBlacklist noopTokenBlacklist() {
		return new TokenBlacklist() {
			@Override
			public boolean isBlacklisted(String jti) {
				return false;
			}

			@Override
			public void blacklistUntil(String jti, Instant expiry) {
				// Redis 미구성 시 서버 무효화는 적용되지 않음
			}
		};
	}
}
