package com.princesses7.findy.user.social.entity;

import java.util.Locale;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;

public enum SocialProvider {
	GOOGLE,
	KAKAO;

	public static SocialProvider from(String value) {
		try {
			return SocialProvider.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (RuntimeException exception) {
			throw new BaseException(ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER);
		}
	}
}
