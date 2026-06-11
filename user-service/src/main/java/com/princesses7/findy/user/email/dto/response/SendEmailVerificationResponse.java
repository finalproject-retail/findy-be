package com.princesses7.findy.user.email.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.princesses7.findy.user.email.service.EmailVerificationPurpose;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendEmailVerificationResponse(
	String email,
	EmailVerificationPurpose purpose,
	LocalDateTime expiresAt,
	String debugCode
) {
}
