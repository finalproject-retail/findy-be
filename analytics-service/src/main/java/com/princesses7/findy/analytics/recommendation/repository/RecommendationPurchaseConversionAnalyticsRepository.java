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
		FROM recommendation_logs
		WHERE created_at >= :fromDateTime
			AND created_at < :toDateTime
			AND (:recommendationType IS NULL OR recommendation_type = :recommendationType)
			AND (:productId IS NULL OR product_id = :productId)
		""", nativeQuery = true)
	RecommendationPurchaseConversionSummaryProjection findSummary(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime
	);

	@Query(value = """
		SELECT
			CAST(created_at AS DATE) AS "analysisDate",
			COUNT(*) AS "impressionCount",
			COALESCE(SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END), 0) AS "clickCount",
			COALESCE(SUM(CASE WHEN is_purchased = TRUE THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM recommendation_logs
		WHERE created_at >= :fromDateTime
			AND created_at < :toDateTime
			AND (:recommendationType IS NULL OR recommendation_type = :recommendationType)
			AND (:productId IS NULL OR product_id = :productId)
		GROUP BY CAST(created_at AS DATE)
		ORDER BY CAST(created_at AS DATE) ASC
		""", nativeQuery = true)
	List<RecommendationPurchaseConversionDailyProjection> findDailyTrends(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime
	);

	@Query(value = """
		SELECT
			recommendation_type AS "recommendationType",
			product_id AS "productId",
			MAX(product_name) AS "productName",
			COUNT(*) AS "impressionCount",
			COALESCE(SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END), 0) AS "clickCount",
			COALESCE(SUM(CASE WHEN is_purchased = TRUE THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM recommendation_logs
		WHERE created_at >= :fromDateTime
			AND created_at < :toDateTime
			AND (:recommendationType IS NULL OR recommendation_type = :recommendationType)
			AND (:productId IS NULL OR product_id = :productId)
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
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime,
		@Param("limit") int limit
	);
}