package com.princesses7.findy.user.social.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
	@NotBlank(message = "소셜 인증 코드는 필수입니다.")
	String code,

	String redirectUri
) {
}
