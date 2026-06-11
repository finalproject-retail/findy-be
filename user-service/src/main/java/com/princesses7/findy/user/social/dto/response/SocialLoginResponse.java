package com.princesses7.findy.user.social.dto.response;

import com.princesses7.findy.user.social.entity.SocialProvider;

public record SocialLoginResponse(
	Long userId,
	String email,
	String name,
	SocialProvider provider,
	boolean isFirstLogin,
	String accessToken
) {
}
