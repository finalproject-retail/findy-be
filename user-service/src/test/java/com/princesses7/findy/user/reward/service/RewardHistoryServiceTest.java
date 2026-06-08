package com.princesses7.findy.user.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.princesses7.findy.user.reward.dto.response.RewardHistoryListResponse;
import com.princesses7.findy.user.reward.entity.RewardHistory;
import com.princesses7.findy.user.reward.repository.RewardHistoryRepository;
import com.princesses7.findy.user.reward.type.RewardHistoryFilter;
import com.princesses7.findy.user.user.entity.UserEntity;
import com.princesses7.findy.user.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RewardHistoryServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RewardHistoryRepository rewardHistoryRepository;

	@InjectMocks
	private RewardHistoryService rewardHistoryService;

	@Test
	@DisplayName("적립 내역 조회 시 구매 적립 이력을 최신순으로 반환한다")
	void getRewardHistoriesReturnsPurchaseRewards() {
		UserEntity user = UserEntity.builder().userId(2L).build();
		RewardHistory history = RewardHistory.createPurchaseReward(user, 101L, 1500L);
		ReflectionTestUtils.setField(history, "rewardHistoryId", 1L);
		ReflectionTestUtils.setField(history, "createdAt", LocalDateTime.of(2026, 4, 10, 12, 0));

		when(userRepository.existsById(2L)).thenReturn(true);
		when(rewardHistoryRepository.findHistories(eq(2L), any(), any(), any(Pageable.class)))
			.thenReturn(List.of(history));

		RewardHistoryListResponse response = rewardHistoryService.getRewardHistories(
			2L,
			LocalDate.of(2026, 4, 1),
			LocalDate.of(2026, 4, 30),
			RewardHistoryFilter.EARNED,
			50
		);

		assertThat(response.count()).isEqualTo(1);
		assertThat(response.histories()).hasSize(1);
		assertThat(response.histories().get(0).id()).isEqualTo(1L);
		assertThat(response.histories().get(0).type()).isEqualTo("earned");
		assertThat(response.histories().get(0).title()).isEqualTo("구매 포인트 지급");
		assertThat(response.histories().get(0).amount()).isEqualTo(1500L);
		assertThat(response.histories().get(0).subtitle()).isEqualTo("주문번호: 101");
	}

	@Test
	@DisplayName("사용·소멸 필터는 포인트 사용 내역만 반환한다")
	void getRewardHistoriesUsedExpiredFilterReturnsUseRewards() {
		UserEntity user = UserEntity.builder().userId(2L).build();
		RewardHistory purchaseHistory = RewardHistory.createPurchaseReward(user, 101L, 1500L);
		RewardHistory useHistory = RewardHistory.createUseReward(user, 102L, 300L);
		ReflectionTestUtils.setField(useHistory, "rewardHistoryId", 2L);
		ReflectionTestUtils.setField(useHistory, "createdAt", LocalDateTime.of(2026, 4, 11, 12, 0));

		when(userRepository.existsById(2L)).thenReturn(true);
		when(rewardHistoryRepository.findHistories(eq(2L), any(), any(), any(Pageable.class)))
			.thenReturn(List.of(purchaseHistory, useHistory));

		RewardHistoryListResponse response = rewardHistoryService.getRewardHistories(
			2L,
			null,
			null,
			RewardHistoryFilter.USED_EXPIRED,
			50
		);

		assertThat(response.count()).isEqualTo(1);
		assertThat(response.histories().get(0).type()).isEqualTo("used");
		assertThat(response.histories().get(0).amount()).isEqualTo(-300L);
	}
}
