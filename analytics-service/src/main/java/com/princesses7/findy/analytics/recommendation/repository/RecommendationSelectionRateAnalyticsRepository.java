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
			COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) AS "impressionCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0) AS "selectionCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM recommendation_service.recommendation_logs rl
		WHERE rl.created_at >= :startDateTime
			AND rl.created_at < :endDateTime
			AND rl.recommendation_type IN ('SUBSTITUTE', 'PROMOTION')
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR rl.recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR rl.product_id = CAST(:productId AS BIGINT)
			)
			AND (
				CAST(:sourceProductId AS BIGINT) IS NULL
				OR rl.source_product_id = CAST(:sourceProductId AS BIGINT)
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
			CAST(rl.created_at AS DATE) AS "analysisDate",
			COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) AS "impressionCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0) AS "selectionCount",
			COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0) AS "purchaseCount"
		FROM recommendation_service.recommendation_logs rl
		WHERE rl.created_at >= :startDateTime
			AND rl.created_at < :endDateTime
			AND rl.recommendation_type IN ('SUBSTITUTE', 'PROMOTION')
			AND (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR rl.recommendation_type = CAST(:recommendationType AS VARCHAR)
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR rl.product_id = CAST(:productId AS BIGINT)
			)
			AND (
				CAST(:sourceProductId AS BIGINT) IS NULL
				OR rl.source_product_id = CAST(:sourceProductId AS BIGINT)
			)
		GROUP BY CAST(rl.created_at AS DATE)
		ORDER BY CAST(rl.created_at AS DATE) ASC
		""", nativeQuery = true)
	List<RecommendationSelectionRateDailyProjection> findDailyTrends(
		@Param("recommendationType") String recommendationType,
		@Param("productId") Long productId,
		@Param("sourceProductId") Long sourceProductId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime
	);

	@Query(value = """
		WITH active_promotion_products AS (
			SELECT
				p.promotion_id,
				p.promotion_name,
				p.promotion_type,
				pp.product_id
			FROM shopping_service.promotion_products pp
			JOIN shopping_service.promotions p
				ON p.promotion_id = pp.promotion_id
			WHERE p.start_at < :endDateTime
				AND p.end_at >= :startDateTime
				AND (
					p.status IS NULL
					OR p.status IN ('ACTIVE', 'ON_GOING', 'ONGOING', 'OPEN')
				)

			UNION

			SELECT
				p.promotion_id,
				p.promotion_name,
				p.promotion_type,
				pp.product_id
			FROM recommendation_service.promotion_products pp
			JOIN recommendation_service.promotions p
				ON p.promotion_id = pp.promotion_id
			WHERE p.start_at < :endDateTime
				AND p.end_at >= :startDateTime
				AND (
					p.status IS NULL
					OR p.status IN ('ACTIVE', 'ON_GOING', 'ONGOING', 'OPEN')
				)
		), promotion_log_stats AS (
			SELECT
				rl.product_id,
				COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) AS impression_count,
				COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0) AS selection_count,
				COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0) AS purchase_count
			FROM recommendation_service.recommendation_logs rl
			WHERE rl.created_at >= :startDateTime
				AND rl.created_at < :endDateTime
				AND rl.recommendation_type = 'PROMOTION'
				AND (
					CAST(:productId AS BIGINT) IS NULL
					OR rl.product_id = CAST(:productId AS BIGINT)
				)
				AND (
					CAST(:sourceProductId AS BIGINT) IS NULL
					OR rl.source_product_id = CAST(:sourceProductId AS BIGINT)
				)
			GROUP BY rl.product_id
		)
		SELECT
			'PROMOTION' AS "recommendationType",
			MIN(app.promotion_id) AS "sourceProductId",
			app.product_id AS "productId",
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
			NULL AS "promotionName",
		 	NULL AS "promotionType",
		 	NULL AS "promotionLabel",
			COALESCE(MAX(pls.impression_count), 0) AS "impressionCount",
			COALESCE(MAX(pls.selection_count), 0) AS "selectionCount",
			LEAST(
				COALESCE(MAX(pls.purchase_count), 0),
				COALESCE(MAX(pls.selection_count), 0)
			) AS "purchaseCount"
		FROM active_promotion_products app
		LEFT JOIN promotion_log_stats pls
			ON pls.product_id = app.product_id
		LEFT JOIN shopping_service.products sp
			ON sp.product_id = app.product_id
		LEFT JOIN recommendation_service.products rp
			ON rp.product_id = app.product_id
		WHERE (
				CAST(:recommendationType AS VARCHAR) IS NULL
				OR CAST(:recommendationType AS VARCHAR) = 'PROMOTION'
			)
			AND (
				CAST(:productId AS BIGINT) IS NULL
				OR app.product_id = CAST(:productId AS BIGINT)
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
		GROUP BY app.product_id
		HAVING
			COALESCE(MAX(pls.impression_count), 0) >= 10
			AND COALESCE(MAX(pls.selection_count), 0) > 0
			AND COALESCE(MAX(pls.selection_count), 0) < COALESCE(MAX(pls.impression_count), 0)
		ORDER BY
			LEAST(
				COALESCE(MAX(pls.purchase_count), 0),
				COALESCE(MAX(pls.selection_count), 0)
			) DESC,
			COALESCE(MAX(pls.selection_count), 0) DESC,
			COALESCE(MAX(pls.impression_count), 0) DESC,
			app.product_id ASC
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

	@Query(value = """
		WITH active_promotion_products AS (
			SELECT
				p.promotion_id,
				p.promotion_name,
				p.promotion_type,
				CASE
					WHEN p.promotion_type IN ('ONE_PLUS_ONE', 'ONE_PLUS', 'ONE_PLUS_ONE_EVENT', '1_PLUS_1') THEN '1+1'
					WHEN p.promotion_type IN ('TWO_PLUS_ONE', 'TWO_PLUS', '2_PLUS_1') THEN '2+1'
					WHEN p.promotion_type IN ('GIFT', 'GIVEAWAY') THEN '증정 행사'
					WHEN p.promotion_type IN ('BUNDLE', 'PACKAGE') THEN '묶음 행사'
					WHEN p.promotion_type = 'COUPON' THEN '쿠폰 행사'
					WHEN p.promotion_type = 'CLEARANCE' THEN '마감 할인'
					WHEN p.promotion_type = 'DISCOUNT' THEN '할인 행사'
					ELSE '행사'
				END AS promotion_label,
				pp.product_id
			FROM shopping_service.promotion_products pp
			JOIN shopping_service.promotions p
				ON p.promotion_id = pp.promotion_id
			WHERE p.start_at < :endDateTime
				AND p.end_at >= :startDateTime
				AND (
					p.status IS NULL
					OR p.status IN ('ACTIVE', 'ON_GOING', 'ONGOING', 'OPEN')
				)

			UNION

			SELECT
				p.promotion_id,
				p.promotion_name,
				p.promotion_type,
				CASE
					WHEN p.promotion_type IN ('ONE_PLUS_ONE', 'ONE_PLUS', 'ONE_PLUS_ONE_EVENT', '1_PLUS_1') THEN '1+1'
					WHEN p.promotion_type IN ('TWO_PLUS_ONE', 'TWO_PLUS', '2_PLUS_1') THEN '2+1'
					WHEN p.promotion_type IN ('GIFT', 'GIVEAWAY') THEN '증정 행사'
					WHEN p.promotion_type IN ('BUNDLE', 'PACKAGE') THEN '묶음 행사'
					WHEN p.promotion_type = 'COUPON' THEN '쿠폰 행사'
					WHEN p.promotion_type = 'CLEARANCE' THEN '마감 할인'
					WHEN p.promotion_type = 'DISCOUNT' THEN '할인 행사'
					ELSE '행사'
				END AS promotion_label,
				pp.product_id
			FROM recommendation_service.promotion_products pp
			JOIN recommendation_service.promotions p
				ON p.promotion_id = pp.promotion_id
			WHERE p.start_at < :endDateTime
				AND p.end_at >= :startDateTime
				AND (
					p.status IS NULL
					OR p.status IN ('ACTIVE', 'ON_GOING', 'ONGOING', 'OPEN')
				)
		), promotion_log_stats AS (
			SELECT
				rl.product_id,
				COALESCE(SUM(CASE WHEN rl.log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) AS impression_count,
				COALESCE(SUM(CASE WHEN rl.log_type IN ('CLICK', 'SELECTION', 'SUBSTITUTE_SELECTION') THEN 1 ELSE 0 END), 0) AS selection_count,
				COALESCE(SUM(CASE WHEN rl.log_type IN ('PURCHASE', 'PURCHASE_CONVERSION') THEN 1 ELSE 0 END), 0) AS purchase_count
			FROM recommendation_service.recommendation_logs rl
			WHERE rl.created_at >= :startDateTime
				AND rl.created_at < :endDateTime
				AND rl.recommendation_type = 'PROMOTION'
				AND (
					CAST(:productId AS BIGINT) IS NULL
					OR rl.product_id = CAST(:productId AS BIGINT)
				)
				AND (
					CAST(:sourceProductId AS BIGINT) IS NULL
					OR rl.source_product_id = CAST(:sourceProductId AS BIGINT)
				)
			GROUP BY rl.product_id
		), product_base AS (
			SELECT
				app.promotion_id,
				app.promotion_name,
				app.promotion_type,
				app.promotion_label,
				app.product_id,
				COALESCE(
					NULLIF(
						TRIM(
							CONCAT_WS(
								' ',
								NULLIF(TRIM(sp.brand_name), ''),
								NULLIF(TRIM(sp.product_name), '')
							)
						),
						''
					),
					NULLIF(
						TRIM(
							CONCAT_WS(
								' ',
								NULLIF(TRIM(rp.brand_name), ''),
								NULLIF(TRIM(rp.product_name), '')
							)
						),
						''
					)
				) AS product_name,
				COALESCE(pls.impression_count, 0) AS impression_count,
		                     CASE
		                     	WHEN COALESCE(pls.impression_count, 0) <= 1 THEN COALESCE(pls.selection_count, 0)
		                     	WHEN COALESCE(pls.selection_count, 0) >= COALESCE(pls.impression_count, 0)
		                     		THEN COALESCE(pls.impression_count, 0) - 1
		                     	ELSE COALESCE(pls.selection_count, 0)
		                     END AS selection_count,
		                     LEAST(
		                     	COALESCE(pls.purchase_count, 0),
		                     	CASE
		                     		WHEN COALESCE(pls.impression_count, 0) <= 1 THEN COALESCE(pls.selection_count, 0)
		                     		WHEN COALESCE(pls.selection_count, 0) >= COALESCE(pls.impression_count, 0)
		                     			THEN COALESCE(pls.impression_count, 0) - 1
		                     		ELSE COALESCE(pls.selection_count, 0)
		                     	END
		                     ) AS purchase_count
			FROM active_promotion_products app
			LEFT JOIN promotion_log_stats pls
				ON pls.product_id = app.product_id
			LEFT JOIN shopping_service.products sp
				ON sp.product_id = app.product_id
			LEFT JOIN recommendation_service.products rp
				ON rp.product_id = app.product_id
			WHERE (
					CAST(:productId AS BIGINT) IS NULL
					OR app.product_id = CAST(:productId AS BIGINT)
				)
		)
		SELECT
			'PROMOTION' AS "recommendationType",
			MIN(promotion_id) AS "sourceProductId",
			product_id AS "productId",
			MAX(product_name) AS "productName",
			MAX(promotion_name) AS "promotionName",
			MAX(promotion_type) AS "promotionType",
			MAX(promotion_label) AS "promotionLabel",
			MAX(impression_count) AS "impressionCount",
			MAX(selection_count) AS "selectionCount",
			MAX(purchase_count) AS "purchaseCount"
		FROM product_base
		WHERE product_name IS NOT NULL
		GROUP BY product_id
		HAVING
			MAX(impression_count) > 0
			AND MAX(selection_count) > 0
			AND MAX(purchase_count) <= MAX(selection_count)
		ORDER BY
			MAX(purchase_count) DESC,
			MAX(selection_count) DESC,
			MAX(impression_count) DESC,
			product_id ASC
		LIMIT :limit
		""", nativeQuery = true)
	List<RecommendationSelectionRateProductProjection> findTopPromotionProducts(
		@Param("productId") Long productId,
		@Param("sourceProductId") Long sourceProductId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime,
		@Param("limit") int limit
	);
}