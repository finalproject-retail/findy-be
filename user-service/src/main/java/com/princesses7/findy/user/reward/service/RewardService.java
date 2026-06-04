package com.princesses7.findy.user.reward.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;
import com.princesses7.findy.user.reward.dto.request.AccruePurchaseRewardRequest;
import com.princesses7.findy.user.reward.dto.response.AccruePurchaseRewardResponse;
import com.princesses7.findy.user.reward.entity.RewardHistory;
import com.princesses7.findy.user.reward.repository.RewardHistoryRepository;
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

		return rewardHistoryRepository.findByOrderId(request.orderId())
			.map(rewardHistory -> toIdempotentResponse(user, rewardHistory))
			.orElseGet(() -> accrueNewPurchaseReward(user, request));
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