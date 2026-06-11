package com.princesses7.findy.user.password.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	String email,

	@NotBlank(message = "새 비밀번호는 필수입니다.")
	@Pattern(
		regexp = "^.{8,16}$",
		message = "비밀번호는 8~16자리여야 합니다."
	)
	String newPassword,

	@NotBlank(message = "새 비밀번호 확인은 필수입니다.")
	String newPasswordConfirm
) {
}
