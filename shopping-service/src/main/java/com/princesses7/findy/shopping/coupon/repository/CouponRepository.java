package com.princesses7.findy.shopping.coupon.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.shopping.coupon.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

	Optional<Coupon> findByCouponId(Long couponId);

	Optional<Coupon> findByCouponIdAndActiveTrue(Long couponId);

	@Query("""
		SELECT c
		FROM Coupon c
		WHERE (:keyword IS NULL OR c.couponName LIKE CONCAT('%', :keyword, '%'))
		AND (:active IS NULL OR c.active = :active)
		""")
	Page<Coupon> searchAdminCoupons(
		@Param("keyword") String keyword,
		@Param("active") Boolean active,
		Pageable pageable
	);

	@Query("""
		SELECT c
		FROM Coupon c
		WHERE c.active = true
		AND c.startAt <= :now
		AND c.endAt >= :now
		""")
	Page<Coupon> findAvailableCoupons(
		@Param("now") LocalDateTime now,
		Pageable pageable
	);
}