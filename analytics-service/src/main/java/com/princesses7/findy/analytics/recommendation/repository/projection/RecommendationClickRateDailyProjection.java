package com.princesses7.findy.analytics.recommendation.repository.projection;

import java.time.LocalDate;

public interface RecommendationClickRateDailyProjection {

	LocalDate getAnalysisDate();

	Long getImpressionCount();

	Long getClickCount();
}