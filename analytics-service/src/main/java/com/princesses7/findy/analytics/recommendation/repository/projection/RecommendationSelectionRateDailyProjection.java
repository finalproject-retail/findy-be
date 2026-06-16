package com.princesses7.findy.analytics.recommendation.repository.projection;

import java.time.LocalDate;

public interface RecommendationSelectionRateDailyProjection {

	LocalDate getAnalysisDate();

	Long getImpressionCount();

	Long getSelectionCount();

	Long getPurchaseCount();
}
