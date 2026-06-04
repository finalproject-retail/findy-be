package com.princesses7.findy.user.reward.entity;

import com.princesses7.findy.user.global.entity.BaseTimeEntity;
import com.princesses7.findy.user.reward.type.RewardType;
import com.princesses7.findy.user.user.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Getter
@Entity
@Table(name = "rewards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardHistory extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reward_history_id")
	private Long rewardHistoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "order_id", unique = true)
	private Long orderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "reward_type", nullable = false, length = 30)
	private RewardType rewardType;

	@Column(name = "reward_amount", nullable = false)
	private long rewardAmount;

	public static RewardHistory createPurchaseReward(UserEntity user, Long orderId, long rewardAmount) {
		RewardHistory rewardHistory = new RewardHistory();
		rewardHistory.user = user;
		rewardHistory.orderId = orderId;
		rewardHistory.rewardType = RewardType.PURCHASE;
		rewardHistory.rewardAmount = rewardAmount;
		return rewardHistory;
	}
}