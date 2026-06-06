package com.princesses7.findy.recommendation.coupon.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CouponProductSnapshotId implements Serializable {

	private Long couponId;
	private Long productId;
}