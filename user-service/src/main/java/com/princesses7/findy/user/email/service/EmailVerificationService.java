package com.princesses7.findy.user.email.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
	private final JavaMailSender javaMailSender;

	@Value("${app.email-verification.debug:true}")
	private boolean debugEnabled;

	@Value("${findy.mail.enabled:false}")
	private boolean mailEnabled;

	@Value("${findy.mail.from:}")
	private String fromEmail;

	@Value("${spring.mail.username:}")
	private String mailUsername;

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

			sendVerificationMail(normalizedEmail, purpose, code);

			log.info(
				"이메일 인증 코드 발송 처리 완료 email={}, purpose={}, mailEnabled={}",
				normalizedEmail,
				purpose,
				mailEnabled
			);

			return new SendEmailVerificationResponse(
				normalizedEmail,
				purpose,
				LocalDateTime.now().plus(CODE_TTL),
				debugEnabled ? code : null
			);
		} catch (MailException e) {
			deleteCodeSafely(codeKey);
			log.error("이메일 인증 메일 발송 실패 email={}, purpose={}", normalizedEmail, purpose, e);
			throw new BaseException(ErrorCode.EMAIL_SEND_FAILED);
		} catch (Exception e) {
			deleteCodeSafely(codeKey);
			log.error("이메일 인증 코드 처리 실패 email={}, purpose={}", normalizedEmail, purpose, e);
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

	private void sendVerificationMail(
		String toEmail,
		EmailVerificationPurpose purpose,
		String code
	) {
		if (!mailEnabled) {
			log.info(
				"메일 발송 비활성화 상태입니다. 실제 메일은 발송하지 않습니다. email={}, purpose={}, code={}",
				toEmail,
				purpose,
				code
			);
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(resolveFromEmail());
		message.setTo(toEmail);
		message.setSubject(createSubject(purpose));
		message.setText(createMailText(purpose, code));

		javaMailSender.send(message);
	}

	private String resolveFromEmail() {
		if (StringUtils.hasText(fromEmail)) {
			return fromEmail.trim();
		}

		if (StringUtils.hasText(mailUsername)) {
			return mailUsername.trim();
		}

		return "no-reply@findy.local";
	}

	private String createSubject(EmailVerificationPurpose purpose) {
		return switch (purpose) {
			case SIGN_UP -> "[Findy] 회원가입 이메일 인증 코드";
			case PASSWORD_RESET -> "[Findy] 비밀번호 재설정 인증 코드";
		};
	}

	private String createMailText(
		EmailVerificationPurpose purpose,
		String code
	) {
		String purposeText = switch (purpose) {
			case SIGN_UP -> "회원가입";
			case PASSWORD_RESET -> "비밀번호 재설정";
		};

		return """
			안녕하세요. Findy입니다.
			
			%s 인증을 위한 이메일 인증 코드입니다.
			
			인증 코드: %s
			
			인증 코드는 5분 동안만 유효합니다.
			본인이 요청하지 않았다면 이 메일을 무시해주세요.
			""".formatted(purposeText, code);
	}

	private void deleteCodeSafely(String codeKey) {
		try {
			stringRedisTemplate.delete(codeKey);
		} catch (Exception e) {
			log.warn("이메일 인증 코드 삭제 실패 codeKey={}", codeKey, e);
		}
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