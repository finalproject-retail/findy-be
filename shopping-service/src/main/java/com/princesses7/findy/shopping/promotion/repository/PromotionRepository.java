package com.princesses7.findy.shopping.promotion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.promotion.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	Optional<Promotion> findByPromotionId(Long promotionId);
}