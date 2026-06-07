package com.princesses7.findy.user.reward.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.user.reward.entity.RewardHistory;

public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {

	Optional<RewardHistory> findByOrderId(Long orderId);

	@Query("""
		select r
		from RewardHistory r
		where r.user.userId = :userId
		and r.createdAt >= :fromDateTime
		and r.createdAt < :toDateTime
		order by r.createdAt desc
		""")
	List<RewardHistory> findHistories(
		@Param("userId") Long userId,
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime,
		Pageable pageable
	);
}