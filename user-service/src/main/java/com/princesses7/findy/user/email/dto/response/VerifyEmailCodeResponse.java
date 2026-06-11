package com.princesses7.findy.user.email.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.user.email.service.EmailVerificationPurpose;

public record VerifyEmailCodeResponse(
	String email,
	EmailVerificationPurpose purpose,
	boolean verified,
	LocalDateTime verifiedExpiresAt
) {
}
