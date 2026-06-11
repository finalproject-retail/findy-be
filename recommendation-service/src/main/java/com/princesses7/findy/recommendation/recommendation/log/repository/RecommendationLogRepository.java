package com.princesses7.findy.recommendation.recommendation.log.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.recommendation.log.entity.RecommendationLog;
import com.princesses7.findy.recommendation.recommendation.log.repository.projection.PopularProductProjection;
import com.princesses7.findy.recommendation.recommendation.log.type.RecommendationLogType;

public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {

	@Query(value = """
		SELECT
			product_id AS "productId",
			COALESCE(SUM(CASE WHEN log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) AS "impressionCount",
			COALESCE(SUM(CASE WHEN log_type = 'CLICK' THEN 1 ELSE 0 END), 0) AS "clickCount",
			COALESCE(SUM(CASE WHEN log_type = 'CLICK' THEN 3 ELSE 0 END), 0)
				+ COALESCE(SUM(CASE WHEN log_type = 'IMPRESSION' THEN 1 ELSE 0 END), 0) AS "popularityScore"
		FROM recommendation_logs
		WHERE created_at >= :fromDateTime
			AND recommendation_type = 'PERSONALIZED'
		GROUP BY product_id
		ORDER BY "popularityScore" DESC, "clickCount" DESC, "impressionCount" DESC, product_id ASC
		LIMIT :limit
		""", nativeQuery = true)
	List<PopularProductProjection> findPopularPersonalizedProducts(
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("limit") int limit
	);

	List<RecommendationLog> findByRecommendationLogIdInAndLogType(
		Collection<Long> recommendationLogIds,
		RecommendationLogType logType
	);

	boolean existsBySourceRecommendationLogIdAndLogType(
		Long sourceRecommendationLogId,
		RecommendationLogType logType
	);

	boolean existsBySourceRecommendationLogIdAndLogTypeAndOrderId(
		Long sourceRecommendationLogId,
		RecommendationLogType logType,
		Long orderId
	);

	Optional<RecommendationLog> findFirstBySourceRecommendationLogIdAndLogTypeAndCreatedAtAfterOrderByCreatedAtDesc(
		Long sourceRecommendationLogId,
		RecommendationLogType logType,
		LocalDateTime createdAtAfter
	);
}