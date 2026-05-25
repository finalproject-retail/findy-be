package com.princesses7.findy.shopping.coupon.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.shopping.coupon.entity.UserCoupon;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

	boolean existsByUserIdAndCoupon_CouponId(Long userId, Long couponId);

	@EntityGraph(attributePaths = "coupon")
	@Query("""
		SELECT uc
		FROM UserCoupon uc
		WHERE uc.userId = :userId
		AND (:used IS NULL OR uc.used = :used)
		AND (
			:expired IS NULL
			OR (:expired = true AND uc.expiresAt < :now)
			OR (:expired = false AND uc.expiresAt >= :now)
		)
		ORDER BY uc.downloadedAt DESC
		""")
	Page<UserCoupon> findMyCoupons(
		@Param("userId") Long userId,
		@Param("used") Boolean used,
		@Param("expired") Boolean expired,
		@Param("now") LocalDateTime now,
		Pageable pageable
	);

	@EntityGraph(attributePaths = "coupon")
	Optional<UserCoupon> findByUserCouponIdAndUserId(Long userCouponId, Long userId);

	List<UserCoupon> findAllByUserIdAndUsedFalse(Long userId);
}