package com.princesses7.findy.recommendation.chatbot.log.repository.projection;

public interface ChatbotLogSummaryProjection {

	Long getTotalCount();

	Long getSuccessCount();

	Long getFailureCount();

	Double getAverageDurationMs();
}