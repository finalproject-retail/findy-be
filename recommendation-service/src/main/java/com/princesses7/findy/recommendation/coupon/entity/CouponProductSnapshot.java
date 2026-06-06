package com.princesses7.findy.recommendation.coupon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coupon_products")
@IdClass(CouponProductSnapshotId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponProductSnapshot {

	@Id
	@Column(name = "coupon_id")
	private Long couponId;

	@Id
	@Column(name = "product_id")
	private Long productId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "coupon_id", insertable = false, updatable = false)
	private CouponSnapshot coupon;
}