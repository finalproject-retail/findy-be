package com.princesses7.findy.user.email.service;

import java.util.Locale;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;

public enum EmailVerificationPurpose {
	PASSWORD_RESET;

	public static EmailVerificationPurpose from(String value) {
		try {
			return EmailVerificationPurpose.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (RuntimeException exception) {
			throw new BaseException(ErrorCode.INVALID_REQUEST, "지원하지 않는 이메일 인증 목적입니다.");
		}
	}
}
