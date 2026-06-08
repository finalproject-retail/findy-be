package com.princesses7.findy.user.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.reward.dto.request.UsePurchaseRewardPublicRequest;
import com.princesses7.findy.user.reward.dto.response.UsePurchaseRewardResponse;
import com.princesses7.findy.user.reward.entity.RewardHistory;
import com.princesses7.findy.user.reward.repository.RewardHistoryRepository;
import com.princesses7.findy.user.reward.type.RewardType;
import com.princesses7.findy.user.user.entity.UserEntity;
import com.princesses7.findy.user.user.entity.UserGradeEntity;
import com.princesses7.findy.user.user.repository.UserGradeRepository;
import com.princesses7.findy.user.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RewardServiceUseTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserGradeRepository userGradeRepository;

	@Mock
	private RewardHistoryRepository rewardHistoryRepository;

	@Mock
	private RewardCalculator rewardCalculator;

	@InjectMocks
	private RewardService rewardService;

	@Test
	@DisplayName("결제 포인트 사용 시 잔액을 차감하고 사용 내역을 저장한다")
	void usePurchaseRewardDeductsBalance() {
		UserGradeEntity grade = UserGradeEntity.builder().rewardRate(0.5).build();
		UserEntity user = UserEntity.builder()
			.userId(2L)
			.reward(1000L)
			.grade(grade)
			.build();
		UsePurchaseRewardPublicRequest request = new UsePurchaseRewardPublicRequest(101L, 300L);

		when(userRepository.findById(2L)).thenReturn(Optional.of(user));
		when(rewardHistoryRepository.findByOrderIdAndRewardType(101L, RewardType.USE))
			.thenReturn(Optional.empty());

		UsePurchaseRewardResponse response = rewardService.usePurchaseRewardFromOrder(2L, request);

		assertThat(response.usedReward()).isEqualTo(300L);
		assertThat(response.rewardBalance()).isEqualTo(700L);
		assertThat(user.getReward()).isEqualTo(700L);
		verify(rewardHistoryRepository).save(any(RewardHistory.class));
	}

	@Test
	@DisplayName("보유 포인트가 부족하면 사용에 실패한다")
	void usePurchaseRewardFailsWhenInsufficientBalance() {
		UserGradeEntity grade = UserGradeEntity.builder().rewardRate(0.5).build();
		UserEntity user = UserEntity.builder()
			.userId(2L)
			.reward(100L)
			.grade(grade)
			.build();
		UsePurchaseRewardPublicRequest request = new UsePurchaseRewardPublicRequest(101L, 300L);

		when(userRepository.findById(2L)).thenReturn(Optional.of(user));
		when(rewardHistoryRepository.findByOrderIdAndRewardType(101L, RewardType.USE))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> rewardService.usePurchaseRewardFromOrder(2L, request))
			.isInstanceOf(BaseException.class);

		verify(rewardHistoryRepository, never()).save(any(RewardHistory.class));
	}
}
