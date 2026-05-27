package com.princesses7.findy.recommendation.promotion.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionStatus;

public interface PromotionProductSnapshotRepository extends JpaRepository<PromotionProductSnapshot, Long> {

	@Query("""
		select pp
		from PromotionProductSnapshot pp
		join fetch pp.promotion p
		where p.status <> :endedStatus
			and p.startAt <= :now
			and p.endAt >= :now
		""")
	List<PromotionProductSnapshot> findActivePromotionProducts(
		@Param("endedStatus") PromotionStatus endedStatus,
		@Param("now") LocalDateTime now
	);
}