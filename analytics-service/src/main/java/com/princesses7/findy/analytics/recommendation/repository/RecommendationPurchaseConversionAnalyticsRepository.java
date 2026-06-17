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
			COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) AS "impressionCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0) AS "clickCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM recommendation_service.recommendation_logs rl
		WHERE rl.created_at >= :startDateTime
			AND rl.created_at < :endDateTime
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR rl.recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR rl.product_id = CAST(:productId AS BIGINT)
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
			CAST(rl.created_at AS DATE) AS "analysisDate",
			COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) AS "impressionCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0) AS "clickCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM recommendation_service.recommendation_logs rl
		WHERE rl.created_at >= :startDateTime
			AND rl.created_at < :endDateTime
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR rl.recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR rl.product_id = CAST(:productId AS BIGINT)
			)
		GROUP BY CAST(rl.created_at AS DATE)
		ORDER BY CAST(rl.created_at AS DATE) ASC
		""", nativeQuery = true)
	List<RecommendationPurchaseConversionDailyProjection> findDailyTrends(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime
	);

	@Query(value = """
		SELECT
			rl.recommendation_type AS "recommendationType",
			rl.product_id AS "productId",
			COALESCE(
				NULLIF(
					TRIM(
						MAX(
							CONCAT_WS(
								' ',
								NULLIF(TRIM(sp.brand_name), ''),
								NULLIF(TRIM(sp.product_name), '')
							)
						)
					),
					''
				),
				NULLIF(
					TRIM(
						MAX(
							CONCAT_WS(
								' ',
								NULLIF(TRIM(rp.brand_name), ''),
								NULLIF(TRIM(rp.product_name), '')
							)
						)
					),
					''
				)
			) AS "productName",
			COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) AS "impressionCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0) AS "clickCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM recommendation_service.recommendation_logs rl
		LEFT JOIN shopping_service.products sp
			ON sp.product_id = rl.product_id
		LEFT JOIN recommendation_service.products rp
			ON rp.product_id = rl.product_id
		WHERE rl.created_at >= :startDateTime
			AND rl.created_at < :endDateTime
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR rl.recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR rl.product_id = CAST(:productId AS BIGINT)
			)
			AND (
				NULLIF(
					TRIM(
						CONCAT_WS(
							' ',
							NULLIF(TRIM(sp.brand_name), ''),
							NULLIF(TRIM(sp.product_name), '')
						)
					),
					''
				) IS NOT NULL
				OR NULLIF(
					TRIM(
						CONCAT_WS(
							' ',
							NULLIF(TRIM(rp.brand_name), ''),
							NULLIF(TRIM(rp.product_name), '')
						)
					),
					''
				) IS NOT NULL
			)
		GROUP BY rl.recommendation_type, rl.product_id
		HAVING
			COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) >= 10
			AND COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0) > 0
			AND COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0) > 0
			AND COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0)
				< COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0)
			AND COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0)
				<= COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0)
		ORDER BY
			COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0) DESC,
			COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0) DESC,
			COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) DESC,
			rl.product_id ASC
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