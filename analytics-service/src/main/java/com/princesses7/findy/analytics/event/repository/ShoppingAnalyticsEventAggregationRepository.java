package com.princesses7.findy.analytics.event.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ShoppingAnalyticsEventAggregationRepository {

	private final JdbcTemplate jdbcTemplate;

	public ShoppingAnalyticsEventAggregationRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean markProcessed(String eventId, String eventType) {
		if (eventId == null || eventId.isBlank()) {
			return true;
		}

		int inserted = jdbcTemplate.update(
			"""
			INSERT INTO kafka_processed_events (event_id, event_type, processed_at)
			VALUES (?, ?, now())
			ON CONFLICT (event_id) DO NOTHING
			""",
			eventId,
			eventType
		);

		return inserted > 0;
	}

	public void incrementProductView(
		Long productId,
		Long categoryId,
		LocalDate analysisDate
	) {
		if (productId == null || analysisDate == null) {
			return;
		}

		jdbcTemplate.update(
			"""
			INSERT INTO product_view_analytics (
			    product_id,
			    category_id,
			    view_count,
			    analysis_date,
			    created_at,
			    updated_at
			)
			VALUES (?, ?, 1, ?, now(), now())
			ON CONFLICT (product_id, analysis_date)
			DO UPDATE SET
			    category_id = COALESCE(product_view_analytics.category_id, EXCLUDED.category_id),
			    view_count = product_view_analytics.view_count + 1,
			    updated_at = now()
			""",
			productId,
			categoryId,
			analysisDate
		);
	}

	public void incrementShoppingListProduct(
		Long productId,
		LocalDate analysisDate,
		long quantity
	) {
		if (productId == null || analysisDate == null || quantity < 1) {
			return;
		}

		jdbcTemplate.update(
			"""
			INSERT INTO shopping_list_product_analytics (
			    product_id,
			    added_count,
			    total_quantity,
			    analysis_date,
			    created_at,
			    updated_at
			)
			VALUES (?, 1, ?, ?, now(), now())
			ON CONFLICT (product_id, analysis_date)
			DO UPDATE SET
			    added_count = shopping_list_product_analytics.added_count + 1,
			    total_quantity = shopping_list_product_analytics.total_quantity + EXCLUDED.total_quantity,
			    updated_at = now()
			""",
			productId,
			quantity,
			analysisDate
		);
	}

	public void insertRecommendationLog(
		Long userId,
		Long productId,
		Long sourceProductId,
		String recommendationType,
		boolean clicked,
		boolean purchased,
		LocalDateTime createdAt
	) {
		if (productId == null || recommendationType == null || recommendationType.isBlank()) {
			return;
		}

		LocalDateTime eventTime = createdAt == null ? LocalDateTime.now() : createdAt;

		jdbcTemplate.update(
			"""
			INSERT INTO recommendation_logs (
			    user_id,
			    product_id,
			    source_product_id,
			    recommendation_type,
			    is_clicked,
			    is_purchased,
			    created_at,
			    updated_at
			)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?)
			""",
			userId,
			productId,
			sourceProductId,
			recommendationType,
			clicked,
			purchased,
			Timestamp.valueOf(eventTime),
			Timestamp.valueOf(eventTime)
		);
	}

	public void incrementRecommendationClickRate(
		String recommendationType,
		Long productId,
		LocalDate analysisDate,
		long impressionDelta,
		long clickDelta
	) {
		if (recommendationType == null || recommendationType.isBlank() || productId == null || analysisDate == null) {
			return;
		}

		jdbcTemplate.update(
			"""
			INSERT INTO recommendation_click_rate_analytics (
			    recommendation_type,
			    product_id,
			    impression_count,
			    click_count,
			    click_rate,
			    analysis_date,
			    created_at,
			    updated_at
			)
			VALUES (?, ?, ?, ?, ?, ?, now(), now())
			ON CONFLICT (recommendation_type, product_id, analysis_date)
			DO UPDATE SET
			    impression_count = recommendation_click_rate_analytics.impression_count + EXCLUDED.impression_count,
			    click_count = recommendation_click_rate_analytics.click_count + EXCLUDED.click_count,
			    click_rate = CASE
			        WHEN recommendation_click_rate_analytics.impression_count + EXCLUDED.impression_count = 0 THEN 0
			        ELSE ROUND(
			            ((recommendation_click_rate_analytics.click_count + EXCLUDED.click_count)::numeric * 100)
			            / (recommendation_click_rate_analytics.impression_count + EXCLUDED.impression_count),
			            2
			        )
			    END,
			    updated_at = now()
			""",
			recommendationType,
			productId,
			impressionDelta,
			clickDelta,
			rate(clickDelta, impressionDelta),
			analysisDate
		);
	}

	public void incrementRecommendationConversion(
		String recommendationType,
		Long productId,
		LocalDate analysisDate,
		long clickDelta,
		long purchaseDelta
	) {
		if (recommendationType == null || recommendationType.isBlank() || productId == null || analysisDate == null) {
			return;
		}

		jdbcTemplate.update(
			"""
			INSERT INTO recommendation_conversion_analytics (
			    recommendation_type,
			    product_id,
			    click_count,
			    purchase_count,
			    conversion_rate,
			    analysis_date,
			    created_at,
			    updated_at
			)
			VALUES (?, ?, ?, ?, ?, ?, now(), now())
			ON CONFLICT (recommendation_type, product_id, analysis_date)
			DO UPDATE SET
			    click_count = recommendation_conversion_analytics.click_count + EXCLUDED.click_count,
			    purchase_count = recommendation_conversion_analytics.purchase_count + EXCLUDED.purchase_count,
			    conversion_rate = CASE
			        WHEN recommendation_conversion_analytics.click_count + EXCLUDED.click_count = 0 THEN 0
			        ELSE ROUND(
			            ((recommendation_conversion_analytics.purchase_count + EXCLUDED.purchase_count)::numeric * 100)
			            / (recommendation_conversion_analytics.click_count + EXCLUDED.click_count),
			            2
			        )
			    END,
			    updated_at = now()
			""",
			recommendationType,
			productId,
			clickDelta,
			purchaseDelta,
			rate(purchaseDelta, clickDelta),
			analysisDate
		);
	}

	public void incrementDailyPurchase(Long storeId, LocalDate metricDate) {
		if (metricDate == null) {
			return;
		}

		jdbcTemplate.update(
			"""
			INSERT INTO daily_operation_metrics (
			    store_id,
			    metric_date,
			    purchase_count,
			    created_at,
			    updated_at
			)
			VALUES (?, ?, 1, now(), now())
			ON CONFLICT (store_id, metric_date)
			DO UPDATE SET
			    purchase_count = daily_operation_metrics.purchase_count + 1,
			    updated_at = now()
			""",
			storeId,
			metricDate
		);
	}

	private BigDecimal rate(long numerator, long denominator) {
		if (denominator == 0) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(numerator)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
	}
}
