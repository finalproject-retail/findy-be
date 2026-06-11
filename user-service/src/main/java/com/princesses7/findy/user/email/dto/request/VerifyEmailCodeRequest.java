package com.princesses7.findy.user.email.dto.request;

import com.princesses7.findy.user.email.service.EmailVerificationPurpose;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyEmailCodeRequest(
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	String email,

	@NotNull(message = "인증 목적은 필수입니다.")
	EmailVerificationPurpose purpose,

	@NotBlank(message = "인증 코드는 필수입니다.")
	String code
) {
}
