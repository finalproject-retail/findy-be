package com.princesses7.findy.user.email.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.princesses7.findy.user.email.dto.response.SendEmailVerificationResponse;
import com.princesses7.findy.user.email.dto.response.VerifyEmailCodeResponse;
import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private static final Duration CODE_TTL = Duration.ofMinutes(5);
	private static final Duration VERIFIED_TTL = Duration.ofMinutes(10);

	private static final String CODE_KEY_PREFIX = "email-verification:code:";
	private static final String VERIFIED_KEY_PREFIX = "email-verification:verified:";

	private final StringRedisTemplate stringRedisTemplate;

	@Value("${app.email-verification.debug:true}")
	private boolean debugEnabled;

	public SendEmailVerificationResponse sendCode(
		String email,
		EmailVerificationPurpose purpose
	) {
		String normalizedEmail = normalizeEmail(email);
		String code = createCode();

		String codeKey = codeKey(normalizedEmail, purpose);
		String verifiedKey = verifiedKey(normalizedEmail, purpose);

		try {
			stringRedisTemplate.opsForValue().set(codeKey, code, CODE_TTL);
			stringRedisTemplate.delete(verifiedKey);

			// TODO: 실제 SMTP 연동 시 여기에서 이메일 발송 처리
			log.info(
				"이메일 인증 코드 발송 email={}, purpose={}, code={}",
				normalizedEmail,
				purpose,
				code
			);

			return new SendEmailVerificationResponse(
				normalizedEmail,
				purpose,
				LocalDateTime.now().plus(CODE_TTL),
				debugEnabled ? code : null
			);
		} catch (Exception e) {
			log.error("이메일 인증 코드 발송 실패 email={}, purpose={}", normalizedEmail, purpose, e);
			throw new BaseException(ErrorCode.EMAIL_SEND_FAILED);
		}
	}

	public VerifyEmailCodeResponse verifyCode(
		String email,
		EmailVerificationPurpose purpose,
		String code
	) {
		String normalizedEmail = normalizeEmail(email);
		String codeKey = codeKey(normalizedEmail, purpose);

		String savedCode = stringRedisTemplate.opsForValue().get(codeKey);

		if (savedCode == null) {
			throw new BaseException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
		}

		if (!savedCode.equals(code)) {
			throw new BaseException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
		}

		String verifiedKey = verifiedKey(normalizedEmail, purpose);

		stringRedisTemplate.opsForValue().set(verifiedKey, "true", VERIFIED_TTL);
		stringRedisTemplate.delete(codeKey);

		return new VerifyEmailCodeResponse(
			normalizedEmail,
			purpose,
			true,
			LocalDateTime.now().plus(VERIFIED_TTL)
		);
	}

	public void validateVerified(
		String email,
		EmailVerificationPurpose purpose
	) {
		String normalizedEmail = normalizeEmail(email);
		String verifiedKey = verifiedKey(normalizedEmail, purpose);

		Boolean verified = stringRedisTemplate.hasKey(verifiedKey);

		if (!Boolean.TRUE.equals(verified)) {
			throw new BaseException(ErrorCode.EMAIL_VERIFICATION_REQUIRED);
		}
	}

	public void consumeVerified(
		String email,
		EmailVerificationPurpose purpose
	) {
		String normalizedEmail = normalizeEmail(email);
		String verifiedKey = verifiedKey(normalizedEmail, purpose);

		stringRedisTemplate.delete(verifiedKey);
	}

	private String createCode() {
		return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String codeKey(String email, EmailVerificationPurpose purpose) {
		return CODE_KEY_PREFIX + purpose.name() + ":" + email;
	}

	private String verifiedKey(String email, EmailVerificationPurpose purpose) {
		return VERIFIED_KEY_PREFIX + purpose.name() + ":" + email;
	}
}