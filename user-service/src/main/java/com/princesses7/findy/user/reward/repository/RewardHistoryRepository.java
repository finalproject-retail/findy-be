package com.princesses7.findy.user.reward.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.user.reward.entity.RewardHistory;

public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {

	Optional<RewardHistory> findByOrderId(Long orderId);
}