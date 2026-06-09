package com.princesses7.findy.analytics.recommendation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.analytics.recommendation.entity.RecommendationLog;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateDailyProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateProductProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateSummaryProjection;

public interface RecommendationSelectionRateAnalyticsRepository extends JpaRepository<RecommendationLog, Long> {

	@Query(value = """
		SELECT
			COUNT(*) AS "impressionCount",
			COALESCE(SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END), 0) AS "selectionCount"
		FROM analytics_service.recommendation_logs
		WHERE created_at >= :startDateTime
			AND created_at < :endDateTime
			AND recommendation_type IN ('SUBSTITUTE', 'PROMOTION')
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR product_id = CAST(:productId AS BIGINT)
			)
			AND (
				CAST(:sourceProductId AS BIGINT) IS NULL
				OR source_product_id = CAST(:sourceProductId AS BIGINT)
			)
		""", nativeQuery = true)
	RecommendationSelectionRateSummaryProjection findSummary(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("sourceProductId") Long sourceProductId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime
	);

	@Query(value = """
		SELECT
			CAST(created_at AS DATE) AS "analysisDate",
			COUNT(*) AS "impressionCount",
			COALESCE(SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END), 0) AS "selectionCount"
		FROM analytics_service.recommendation_logs
		WHERE created_at >= :startDateTime
			AND created_at < :endDateTime
			AND recommendation_type IN ('SUBSTITUTE', 'PROMOTION')
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR product_id = CAST(:productId AS BIGINT)
			)
			AND (
				CAST(:sourceProductId AS BIGINT) IS NULL
				OR source_product_id = CAST(:sourceProductId AS BIGINT)
			)
		GROUP BY CAST(created_at AS DATE)
		ORDER BY CAST(created_at AS DATE) ASC
		""", nativeQuery = true)
	List<RecommendationSelectionRateDailyProjection> findDailyTrends(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("sourceProductId") Long sourceProductId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime
	);

	@Query(value = """
		SELECT
			recommendation_type AS "recommendationType",
			source_product_id AS "sourceProductId",
			product_id AS "productId",
			MAX(product_name) AS "productName",
			COUNT(*) AS "impressionCount",
			COALESCE(SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END), 0) AS "selectionCount"
		FROM analytics_service.recommendation_logs
		WHERE created_at >= :startDateTime
			AND created_at < :endDateTime
			AND recommendation_type IN ('SUBSTITUTE', 'PROMOTION')
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR product_id = CAST(:productId AS BIGINT)
			)
			AND (
				CAST(:sourceProductId AS BIGINT) IS NULL
				OR source_product_id = CAST(:sourceProductId AS BIGINT)
			)
		GROUP BY recommendation_type, source_product_id, product_id
		ORDER BY
			COALESCE(SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END), 0) DESC,
			COUNT(*) DESC,
			product_id ASC
		LIMIT :limit
		""", nativeQuery = true)
	List<RecommendationSelectionRateProductProjection> findTopProducts(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("sourceProductId") Long sourceProductId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime,
		@Param("limit") int limit
	);
}