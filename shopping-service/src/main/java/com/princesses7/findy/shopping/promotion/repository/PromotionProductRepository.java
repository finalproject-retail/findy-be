package com.princesses7.findy.shopping.promotion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {

	boolean existsByPromotion_PromotionIdAndProductId(
		Long promotionId,
		Long productId
	);

	Optional<PromotionProduct> findByPromotionProductId(Long promotionProductId);

	List<PromotionProduct> findAllByPromotion_PromotionId(Long promotionId);
}