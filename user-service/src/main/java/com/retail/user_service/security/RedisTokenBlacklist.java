package com.retail.user_service.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.retail.user_service.global.exception.BaseException;
import com.retail.user_service.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RedisTokenBlacklist implements TokenBlacklist {

    private static final String KEY_PREFIX = "user-service:jwt:bl:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isBlacklisted(String jti) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
        } catch (Exception e) {
            log.error("Redis blacklist check failed. jti={}", jti, e);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "인증 서버에 일시적인 오류가 있습니다.");
        }
    }

    @Override
    public void blacklistUntil(String jti, Instant expiry) {
        Duration ttl = Duration.between(Instant.now(), expiry);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", ttl);
            log.info("Token blacklisted in Redis. key={}, ttlSeconds={}", KEY_PREFIX + jti, ttl.toSeconds());
        } catch (Exception e) {
            log.error("Redis blacklist write failed. jti={}", jti, e);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "로그아웃 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
}
