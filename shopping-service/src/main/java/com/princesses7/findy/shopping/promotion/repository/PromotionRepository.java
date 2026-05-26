package com.princesses7.findy.shopping.promotion.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.promotion.entity.Promotion;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	Optional<Promotion> findByPromotionId(Long promotionId);

	Page<Promotion> findByPromotionNameContainingIgnoreCase(
		String keyword,
		Pageable pageable
	);

	Page<Promotion> findByStatus(
		PromotionStatus status,
		Pageable pageable
	);

	Page<Promotion> findByPromotionNameContainingIgnoreCaseAndStatus(
		String keyword,
		PromotionStatus status,
		Pageable pageable
	);
}