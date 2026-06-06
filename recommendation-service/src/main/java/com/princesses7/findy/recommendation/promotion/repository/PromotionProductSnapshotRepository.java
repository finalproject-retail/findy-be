package com.princesses7.findy.recommendation.promotion.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionStatus;

public interface PromotionProductSnapshotRepository extends JpaRepository<PromotionProductSnapshot, Long> {

	@Query("""
		SELECT pp
		FROM PromotionProductSnapshot pp
		JOIN FETCH pp.promotion p
		WHERE p.status <> :endedStatus
			AND p.startAt <= :now
			AND p.endAt >= :now
		""")
	List<PromotionProductSnapshot> findActivePromotionProducts(
		@Param("endedStatus") PromotionStatus endedStatus,
		@Param("now") LocalDateTime now
	);

	@Query("""
		SELECT pp
		FROM PromotionProductSnapshot pp
		JOIN FETCH pp.promotion p
		WHERE pp.productId IN :productIds
			AND p.status <> :endedStatus
			AND p.startAt <= :now
			AND p.endAt >= :now
		""")
	List<PromotionProductSnapshot> findActivePromotionProductsByProductIds(
		@Param("productIds") Collection<Long> productIds,
		@Param("endedStatus") PromotionStatus endedStatus,
		@Param("now") LocalDateTime now
	);
}