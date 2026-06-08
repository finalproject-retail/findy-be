package com.princesses7.findy.user.reward.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;
import com.princesses7.findy.user.reward.dto.request.AccruePurchaseRewardPublicRequest;
import com.princesses7.findy.user.reward.dto.request.AccruePurchaseRewardRequest;
import com.princesses7.findy.user.reward.dto.request.UsePurchaseRewardPublicRequest;
import com.princesses7.findy.user.reward.dto.response.AccruePurchaseRewardResponse;
import com.princesses7.findy.user.reward.dto.response.UsePurchaseRewardResponse;
import com.princesses7.findy.user.reward.entity.RewardHistory;
import com.princesses7.findy.user.reward.repository.RewardHistoryRepository;
import com.princesses7.findy.user.reward.type.RewardType;
import com.princesses7.findy.user.user.entity.UserEntity;
import com.princesses7.findy.user.user.entity.UserGradeEntity;
import com.princesses7.findy.user.user.repository.UserGradeRepository;
import com.princesses7.findy.user.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RewardService {

	private final UserRepository userRepository;
	private final UserGradeRepository userGradeRepository;
	private final RewardHistoryRepository rewardHistoryRepository;
	private final RewardCalculator rewardCalculator;

	@Transactional
	public AccruePurchaseRewardResponse accruePurchaseReward(
		Long userId,
		AccruePurchaseRewardRequest request
	) {
		UserEntity user = getActiveUser(userId);

		return rewardHistoryRepository.findByOrderIdAndRewardType(request.orderId(), RewardType.PURCHASE)
			.map(rewardHistory -> toIdempotentResponse(user, rewardHistory))
			.orElseGet(() -> accrueNewPurchaseReward(user, request));
	}

	@Transactional
	public UsePurchaseRewardResponse usePurchaseRewardFromOrder(
		Long userId,
		UsePurchaseRewardPublicRequest request
	) {
		UserEntity user = getActiveUser(userId);

		return rewardHistoryRepository.findByOrderIdAndRewardType(request.orderId(), RewardType.USE)
			.map(rewardHistory -> toUseIdempotentResponse(user, rewardHistory))
			.orElseGet(() -> useNewPurchaseReward(user, request));
	}

	@Transactional
	public AccruePurchaseRewardResponse accruePurchaseRewardFromOrder(
		Long userId,
		AccruePurchaseRewardPublicRequest request
	) {
		UserEntity user = getActiveUser(userId);

		return accruePurchaseReward(
			userId,
			new AccruePurchaseRewardRequest(
				request.orderId(),
				request.finalAmount(),
				user.getPurchaseAmount() + request.finalAmount()
			)
		);
	}

	private AccruePurchaseRewardResponse accrueNewPurchaseReward(
		UserEntity user,
		AccruePurchaseRewardRequest request
	) {
		double rewardRate = user.getGrade().getRewardRate();
		long earnedReward = rewardCalculator.calculate(request.finalAmount(), rewardRate);
		UserGradeEntity nextGrade = resolveGrade(request.gradeBaseAmount());

		RewardHistory rewardHistory = RewardHistory.createPurchaseReward(
			user,
			request.orderId(),
			earnedReward
		);

		user.applyPurchaseReward(request.gradeBaseAmount(), earnedReward, nextGrade);
		rewardHistoryRepository.save(rewardHistory);

		return toResponse(user, rewardHistory);
	}

	private UsePurchaseRewardResponse useNewPurchaseReward(
		UserEntity user,
		UsePurchaseRewardPublicRequest request
	) {
		if (user.getReward() < request.usedAmount()) {
			throw new BaseException(ErrorCode.INSUFFICIENT_REWARD_BALANCE);
		}

		RewardHistory rewardHistory = RewardHistory.createUseReward(
			user,
			request.orderId(),
			request.usedAmount()
		);

		user.useReward(request.usedAmount());
		rewardHistoryRepository.save(rewardHistory);

		return toUseResponse(user, rewardHistory);
	}

	private UsePurchaseRewardResponse toUseIdempotentResponse(UserEntity user, RewardHistory rewardHistory) {
		if (!rewardHistory.getUser().getUserId().equals(user.getUserId())) {
			throw new BaseException(ErrorCode.INVALID_REQUEST, "해당 주문의 포인트 사용 이력이 다른 회원에게 연결되어 있습니다.");
		}

		return toUseResponse(user, rewardHistory);
	}

	private UsePurchaseRewardResponse toUseResponse(UserEntity user, RewardHistory rewardHistory) {
		return new UsePurchaseRewardResponse(
			user.getUserId(),
			rewardHistory.getOrderId(),
			rewardHistory.getRewardAmount(),
			user.getReward()
		);
	}

	private AccruePurchaseRewardResponse toIdempotentResponse(UserEntity user, RewardHistory rewardHistory) {
		if (!rewardHistory.getUser().getUserId().equals(user.getUserId())) {
			throw new BaseException(ErrorCode.INVALID_REQUEST, "해당 주문의 적립 이력이 다른 회원에게 연결되어 있습니다.");
		}

		return toResponse(user, rewardHistory);
	}

	private UserEntity getActiveUser(Long userId) {
		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

		if (user.getDeletedAt() != null) {
			throw new BaseException(ErrorCode.DELETED_USER);
		}

		return user;
	}

	private UserGradeEntity resolveGrade(long purchaseAmount) {
		return userGradeRepository.findTopByCriteriaAmountLessThanEqualOrderByCriteriaAmountDesc(purchaseAmount)
			.orElseThrow(() -> new BaseException(ErrorCode.USER_GRADE_NOT_FOUND));
	}

	private AccruePurchaseRewardResponse toResponse(UserEntity user, RewardHistory rewardHistory) {
		return new AccruePurchaseRewardResponse(
			user.getUserId(),
			rewardHistory.getOrderId(),
			rewardHistory.getRewardAmount(),
			user.getReward(),
			user.getPurchaseAmount(),
			user.getGrade().getGradeName(),
			user.getGrade().getRewardRate()
		);
	}
}