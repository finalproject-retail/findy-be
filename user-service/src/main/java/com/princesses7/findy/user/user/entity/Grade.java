package com.princesses7.findy.user.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Grade {
	BRONZE, SILVER, GOLD, VIP;

	@JsonCreator
	public static Grade from(String value) {
		return Grade.valueOf(value.trim().toUpperCase());
	}
}
