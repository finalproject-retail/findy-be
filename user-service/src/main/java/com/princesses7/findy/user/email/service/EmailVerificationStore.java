package com.princesses7.findy.user.email.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailVerificationStore {

	private static final String CODE_PREFIX = "email-verification:code:";
	private static final String VERIFIED_PREFIX = "email-verification:verified:";

	private final StringRedisTemplate redisTemplate;

	public void saveCode(String email, EmailVerificationPurpose purpose, String code, Duration ttl) {
		redisTemplate.opsForValue().set(codeKey(email, purpose), code, ttl);
	}

	public String getCode(String email, EmailVerificationPurpose purpose) {
		return redisTemplate.opsForValue().get(codeKey(email, purpose));
	}

	public void deleteCode(String email, EmailVerificationPurpose purpose) {
		redisTemplate.delete(codeKey(email, purpose));
	}

	public void markVerified(String email, EmailVerificationPurpose purpose, Duration ttl) {
		redisTemplate.opsForValue().set(verifiedKey(email, purpose), "true", ttl);
	}

	public boolean isVerified(String email, EmailVerificationPurpose purpose) {
		return "true".equals(redisTemplate.opsForValue().get(verifiedKey(email, purpose)));
	}

	public void deleteVerified(String email, EmailVerificationPurpose purpose) {
		redisTemplate.delete(verifiedKey(email, purpose));
	}

	private String codeKey(String email, EmailVerificationPurpose purpose) {
		return CODE_PREFIX + purpose.name() + ":" + normalize(email);
	}

	private String verifiedKey(String email, EmailVerificationPurpose purpose) {
		return VERIFIED_PREFIX + purpose.name() + ":" + normalize(email);
	}

	private String normalize(String email) {
		return email.trim().toLowerCase();
	}
}
