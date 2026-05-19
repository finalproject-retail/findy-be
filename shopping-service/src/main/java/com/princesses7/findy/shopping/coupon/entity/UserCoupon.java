package com.princesses7.findy.shopping.coupon.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.coupon.exception.CouponException;
import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_coupon_id")
	private Long userCouponId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "coupon_id", nullable = false)
	private Coupon coupon;

	@Column(name = "is_used", nullable = false)
	private boolean used;

	@Column(name = "downloaded_at", nullable = false)
	private LocalDateTime downloadedAt;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "used_at")
	private LocalDateTime usedAt;

	private UserCoupon(
		Long userId,
		Coupon coupon,
		LocalDateTime downloadedAt,
		LocalDateTime expiresAt
	) {
		this.userId = userId;
		this.coupon = coupon;
		this.used = false;
		this.downloadedAt = downloadedAt;
		this.expiresAt = expiresAt;
	}

	public static UserCoupon create(
		Long userId,
		Coupon coupon,
		LocalDateTime downloadedAt
	) {
		return new UserCoupon(
			userId,
			coupon,
			downloadedAt,
			coupon.calculateExpiresAt(downloadedAt)
		);
	}

	public boolean isExpired(LocalDateTime now) {
		return now.isAfter(expiresAt);
	}

	public void use(LocalDateTime now) {
		if (used) {
			throw new CouponException(COUPON_ALREADY_USED);
		}

		if (isExpired(now)) {
			throw new CouponException(COUPON_EXPIRED);
		}

		this.used = true;
		this.usedAt = now;
	}
}