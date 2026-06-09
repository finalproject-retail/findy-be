package com.princesses7.findy.analytics.recommendation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.analytics.recommendation.entity.RecommendationLog;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationPurchaseConversionDailyProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationPurchaseConversionProductProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationPurchaseConversionSummaryProjection;

public interface RecommendationPurchaseConversionAnalyticsRepository extends JpaRepository<RecommendationLog, Long> {

	@Query(value = """
		SELECT
			COUNT(*) AS "impressionCount",
			COALESCE(SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END), 0) AS "clickCount",
			COALESCE(SUM(CASE WHEN is_purchased = TRUE THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM analytics_service.recommendation_logs
		WHERE created_at >= :startDateTime
			AND created_at < :endDateTime
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR product_id = CAST(:productId AS BIGINT)
			)
		""", nativeQuery = true)
	RecommendationPurchaseConversionSummaryProjection findSummary(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime
	);

	@Query(value = """
		SELECT
			CAST(created_at AS DATE) AS "analysisDate",
			COUNT(*) AS "impressionCount",
			COALESCE(SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END), 0) AS "clickCount",
			COALESCE(SUM(CASE WHEN is_purchased = TRUE THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM analytics_service.recommendation_logs
		WHERE created_at >= :startDateTime
			AND created_at < :endDateTime
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR product_id = CAST(:productId AS BIGINT)
			)
		GROUP BY CAST(created_at AS DATE)
		ORDER BY CAST(created_at AS DATE) ASC
		""", nativeQuery = true)
	List<RecommendationPurchaseConversionDailyProjection> findDailyTrends(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime
	);

	@Query(value = """
		SELECT
			recommendation_type AS "recommendationType",
			product_id AS "productId",
			MAX(product_name) AS "productName",
			COUNT(*) AS "impressionCount",
			COALESCE(SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END), 0) AS "clickCount",
			COALESCE(SUM(CASE WHEN is_purchased = TRUE THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM analytics_service.recommendation_logs
		WHERE created_at >= :startDateTime
			AND created_at < :endDateTime
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR product_id = CAST(:productId AS BIGINT)
			)
		GROUP BY recommendation_type, product_id
		ORDER BY
			COALESCE(SUM(CASE WHEN is_purchased = TRUE THEN 1 ELSE 0 END), 0) DESC,
			COUNT(*) DESC,
			product_id ASC
		LIMIT :limit
		""", nativeQuery = true)
	List<RecommendationPurchaseConversionProductProjection> findTopProducts(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime,
		@Param("limit") int limit
	);
}