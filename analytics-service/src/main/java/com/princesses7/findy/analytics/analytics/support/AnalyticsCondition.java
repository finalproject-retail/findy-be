package com.princesses7.findy.analytics.analytics.support;

public record AnalyticsCondition(
	Long storeId,
	Long userId,
	String recommendationType,
	PeriodRange period,
	int limit
) {
}