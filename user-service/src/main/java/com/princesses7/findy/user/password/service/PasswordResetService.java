package com.princesses7.findy.user.password.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.user.email.dto.response.SendEmailVerificationResponse;
import com.princesses7.findy.user.email.dto.response.VerifyEmailCodeResponse;
import com.princesses7.findy.user.email.service.EmailVerificationPurpose;
import com.princesses7.findy.user.email.service.EmailVerificationService;
import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;
import com.princesses7.findy.user.user.entity.UserEntity;
import com.princesses7.findy.user.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private final UserRepository userRepository;
	private final EmailVerificationService emailVerificationService;
	private final PasswordEncoder passwordEncoder;

	public SendEmailVerificationResponse sendPasswordResetCode(String email) {
		assertExistingUser(email);
		return emailVerificationService.sendCode(email, EmailVerificationPurpose.PASSWORD_RESET);
	}

	public VerifyEmailCodeResponse verifyPasswordResetCode(String email, String code) {
		assertExistingUser(email);
		return emailVerificationService.verifyCode(email, EmailVerificationPurpose.PASSWORD_RESET, code);
	}

	@Transactional
	public void resetPassword(String email, String newPassword, String newPasswordConfirm) {
		if (!newPassword.equals(newPasswordConfirm)) {
			throw new BaseException(ErrorCode.PASSWORD_MISMATCH);
		}

		UserEntity user = userRepository.findByEmail(email)
			.orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND, "가입되지 않은 이메일입니다."));

		emailVerificationService.validateVerified(email, EmailVerificationPurpose.PASSWORD_RESET);

		user.changePassword(passwordEncoder.encode(newPassword));

		emailVerificationService.consumeVerified(email, EmailVerificationPurpose.PASSWORD_RESET);
	}

	private void assertExistingUser(String email) {
		if (!userRepository.existsByEmail(email)) {
			throw new BaseException(ErrorCode.USER_NOT_FOUND, "가입되지 않은 이메일입니다.");
		}
	}
}