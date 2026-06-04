package com.princesses7.findy.user.user.dto.response;

import com.princesses7.findy.user.user.entity.Grade;
import com.princesses7.findy.user.user.entity.UserEntity;

public record MyPageResponse(
	Long userId,
	String name,
	String email,
	Grade grade,
	long reward
) {

	public static MyPageResponse from(UserEntity user) {
		return new MyPageResponse(
			user.getUserId(),
			user.getName(),
			user.getEmail(),
			user.getGrade().getGradeName(),
			user.getReward()
		);
	}
}
