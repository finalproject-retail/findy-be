package com.princesses7.findy.user.email.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.princesses7.findy.user.email.dto.response.SendEmailVerificationResponse;
import com.princesses7.findy.user.email.dto.response.VerifyEmailCodeResponse;
import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final EmailVerificationStore verificationStore;
	private final EmailSender emailSender;

	@Value("${findy.email-verification.code-length:6}")
	private int codeLength;

	@Value("${findy.email-verification.expire-minutes:5}")
	private long expireMinutes;

	@Value("${findy.email-verification.verified-expire-minutes:10}")
	private long verifiedExpireMinutes;

	@Value("${findy.mail.enabled:false}")
	private boolean mailEnabled;

	public SendEmailVerificationResponse sendCode(String email, EmailVerificationPurpose purpose) {
		String normalizedEmail = normalize(email);
		String code = generateCode();
		Duration ttl = Duration.ofMinutes(expireMinutes);

		verificationStore.saveCode(normalizedEmail, purpose, code, ttl);
		verificationStore.deleteVerified(normalizedEmail, purpose);
		emailSender.send(normalizedEmail, buildSubject(purpose), buildText(code));

		return new SendEmailVerificationResponse(
			normalizedEmail,
			purpose,
			LocalDateTime.now().plus(ttl),
			mailEnabled ? null : code
		);
	}

	public VerifyEmailCodeResponse verifyCode(String email, EmailVerificationPurpose purpose, String code) {
		String normalizedEmail = normalize(email);
		String savedCode = verificationStore.getCode(normalizedEmail, purpose);
		if (savedCode == null) {
			throw new BaseException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
		}
		if (!savedCode.equals(code.trim())) {
			throw new BaseException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
		}

		Duration verifiedTtl = Duration.ofMinutes(verifiedExpireMinutes);
		verificationStore.deleteCode(normalizedEmail, purpose);
		verificationStore.markVerified(normalizedEmail, purpose, verifiedTtl);

		return new VerifyEmailCodeResponse(
			normalizedEmail,
			purpose,
			true,
			LocalDateTime.now().plus(verifiedTtl)
		);
	}

	public void validateVerified(String email, EmailVerificationPurpose purpose) {
		if (!verificationStore.isVerified(normalize(email), purpose)) {
			throw new BaseException(ErrorCode.EMAIL_VERIFICATION_REQUIRED);
		}
	}

	public void consumeVerified(String email, EmailVerificationPurpose purpose) {
		verificationStore.deleteVerified(normalize(email), purpose);
	}

	private String buildSubject(EmailVerificationPurpose purpose) {
		return switch (purpose) {
			case PASSWORD_RESET -> "[Findy] 비밀번호 재설정 인증 코드";
		};
	}

	private String buildText(String code) {
		return "Findy 인증 코드: " + code + "\n" + expireMinutes + "분 안에 입력해 주세요.";
	}

	private String generateCode() {
		int length = Math.max(codeLength, 6);
		StringBuilder builder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			builder.append(RANDOM.nextInt(10));
		}
		return builder.toString();
	}

	private String normalize(String email) {
		return email.trim().toLowerCase();
	}
}
