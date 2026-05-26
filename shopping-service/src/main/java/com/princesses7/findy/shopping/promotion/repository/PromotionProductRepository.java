package com.princesses7.findy.shopping.promotion.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {

	boolean existsByPromotion_PromotionIdAndProductId(
		Long promotionId,
		Long productId
	);

	Optional<PromotionProduct> findByPromotionProductId(Long promotionProductId);

	List<PromotionProduct> findAllByPromotion_PromotionId(Long promotionId);

	@Query(
		value = """
			select pp
			from PromotionProduct pp
			join fetch pp.promotion p
			where p.status <> :endedStatus
				and p.startAt <= :now
				and p.endAt >= :now
			""",
		countQuery = """
			select count(pp)
			from PromotionProduct pp
			join pp.promotion p
			where p.status <> :endedStatus
				and p.startAt <= :now
				and p.endAt >= :now
			"""
	)
	Page<PromotionProduct> findActivePromotionProducts(
		@Param("endedStatus") PromotionStatus endedStatus,
		@Param("now") LocalDateTime now,
		Pageable pageable
	);

	@Query("""
		select pp
		from PromotionProduct pp
		join fetch pp.promotion p
		where pp.productId = :productId
			and p.status <> :endedStatus
			and p.startAt <= :now
			and p.endAt >= :now
		""")
	List<PromotionProduct> findApplicablePromotionProducts(
		@Param("productId") Long productId,
		@Param("endedStatus") PromotionStatus endedStatus,
		@Param("now") LocalDateTime now
	);
}