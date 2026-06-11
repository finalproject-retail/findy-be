package com.princesses7.findy.user.social.client;

import com.princesses7.findy.user.social.dto.response.SocialUserProfile;
import com.princesses7.findy.user.social.entity.SocialProvider;

public interface SocialOAuthClient {

	boolean supports(SocialProvider provider);

	SocialUserProfile fetchProfile(String authorizationCode, String redirectUri);
}
