package com.princesses7.findy.recommendation.chatbot.log.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotLogSummaryProjection;

public record AdminChatbotSummaryResponse(
	Long totalCount,
	Long successCount,
	Long failureCount,
	BigDecimal failureRate,
	Double averageDurationMs,
	List<AdminChatbotIntentCountResponse> intentCounts
) {

	public static AdminChatbotSummaryResponse of(
		ChatbotLogSummaryProjection summary,
		List<AdminChatbotIntentCountResponse> intentCounts
	) {
		Long totalCount = valueOrZero(summary.getTotalCount());
		Long successCount = valueOrZero(summary.getSuccessCount());
		Long failureCount = valueOrZero(summary.getFailureCount());

		return new AdminChatbotSummaryResponse(
			totalCount,
			successCount,
			failureCount,
			calculateFailureRate(totalCount, failureCount),
			summary.getAverageDurationMs() == null ? 0.0 : summary.getAverageDurationMs(),
			intentCounts
		);
	}

	private static Long valueOrZero(Long value) {
		return value == null ? 0L : value;
	}

	private static BigDecimal calculateFailureRate(
		Long totalCount,
		Long failureCount
	) {
		if (totalCount == null || totalCount == 0) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(failureCount)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
	}
}