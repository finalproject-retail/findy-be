package com.princesses7.findy.analytics.congestion.support;

public enum ZoneCongestionLevel {

	// TODO: 추후 주석 해제 및 기준치 수정
	EMPTY("비어 있음"),
	// LOW("여유"),
	// NORMAL("보통"),
	// HIGH("혼잡"),
	VERY_HIGH("매우 혼잡");

	private final String description;

	ZoneCongestionLevel(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public static ZoneCongestionLevel fromUserCount(long userCount) {
		if (userCount <= 0) {
			return EMPTY;
		}

		// if (userCount <= 5) {
		// 	return LOW;
		// }
		//
		// if (userCount <= 10) {
		// 	return NORMAL;
		// }
		//
		// if (userCount <= 15) {
		// 	return HIGH;
		// }

		return VERY_HIGH;
	}
}