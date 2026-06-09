package com.princesses7.findy.analytics.recommendation.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.analytics.recommendation.entity.RecommendationClickRateAnalytics;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationClickRateDailyProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationClickRateProductProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationClickRateSummaryProjection;

public interface RecommendationClickRateAnalyticsRepository
	extends JpaRepository<RecommendationClickRateAnalytics, Long> {

	@Query("""
		SELECT
			COALESCE(SUM(r.impressionCount), 0) AS impressionCount,
			COALESCE(SUM(r.clickCount), 0) AS clickCount
		FROM RecommendationClickRateAnalytics r
		WHERE r.analysisDate BETWEEN :fromDate AND :toDate
			AND (:recommendationType IS NULL OR r.recommendationType = :recommendationType)
		""")
	RecommendationClickRateSummaryProjection findSummary(
		@Param("recommendationType") String recommendationType,
		@Param("startDate") LocalDate fromDate,
		@Param("endDate") LocalDate toDate
	);

	@Query("""
		SELECT
			r.analysisDate AS analysisDate,
			COALESCE(SUM(r.impressionCount), 0) AS impressionCount,
			COALESCE(SUM(r.clickCount), 0) AS clickCount
		FROM RecommendationClickRateAnalytics r
		WHERE r.analysisDate BETWEEN :fromDate AND :toDate
			AND (:recommendationType IS NULL OR r.recommendationType = :recommendationType)
		GROUP BY r.analysisDate
		ORDER BY r.analysisDate ASC
		""")
	List<RecommendationClickRateDailyProjection> findDailyTrend(
		@Param("recommendationType") String recommendationType,
		@Param("startDate") LocalDate fromDate,
		@Param("endDate") LocalDate toDate
	);

	@Query("""
		SELECT
			r.recommendationType AS recommendationType,
			r.productId AS productId,
			r.productName AS productName,
			COALESCE(SUM(r.impressionCount), 0) AS impressionCount,
			COALESCE(SUM(r.clickCount), 0) AS clickCount
		FROM RecommendationClickRateAnalytics r
		WHERE r.analysisDate BETWEEN :fromDate AND :toDate
			AND (:recommendationType IS NULL OR r.recommendationType = :recommendationType)
		GROUP BY r.recommendationType, r.productId, r.productName
		ORDER BY
			COALESCE(SUM(r.clickCount), 0) DESC,
			COALESCE(SUM(r.impressionCount), 0) DESC,
			r.productId ASC
		""")
	List<RecommendationClickRateProductProjection> findTopProducts(
		@Param("recommendationType") String recommendationType,
		@Param("startDate") LocalDate fromDate,
		@Param("endDate") LocalDate toDate,
		Pageable pageable
	);
}