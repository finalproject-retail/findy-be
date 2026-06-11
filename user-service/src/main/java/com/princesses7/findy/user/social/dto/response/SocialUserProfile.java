package com.princesses7.findy.user.social.dto.response;

import com.princesses7.findy.user.social.entity.SocialProvider;

public record SocialUserProfile(
	SocialProvider provider,
	String providerUserId,
	String email,
	String name
) {
}
