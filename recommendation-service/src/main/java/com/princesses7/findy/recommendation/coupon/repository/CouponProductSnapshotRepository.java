package com.princesses7.findy.recommendation.coupon.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.coupon.entity.CouponProductSnapshot;
import com.princesses7.findy.recommendation.coupon.entity.CouponProductSnapshotId;

public interface CouponProductSnapshotRepository extends JpaRepository<CouponProductSnapshot, CouponProductSnapshotId> {

	@Query("""
		SELECT cp
		FROM CouponProductSnapshot cp
		JOIN FETCH cp.coupon c
		WHERE cp.productId IN :productIds
			AND c.active = TRUE
			AND c.startAt <= :now
			AND c.endAt >= :now
		""")
	List<CouponProductSnapshot> findAvailableCouponProductsByProductIds(
		@Param("productIds") Collection<Long> productIds,
		@Param("now") LocalDateTime now
	);

	@Query("""
		SELECT cp
		FROM CouponProductSnapshot cp
		JOIN FETCH cp.coupon c
		WHERE c.active = TRUE
			AND c.startAt <= :now
			AND c.endAt >= :now
		""")
	List<CouponProductSnapshot> findAvailableCouponProducts(
		@Param("now") LocalDateTime now
	);
}