package com.princesses7.findy.shopping.promotion.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

	@Query("""
		select pp
		from PromotionProduct pp
		join fetch pp.promotion p
		where p.status <> :endedStatus
			and p.startAt <= :now
			and p.endAt >= :now
		order by pp.gridId asc, pp.promotionProductId asc
		""")
	List<PromotionProduct> findActivePromotionMapMarkers(
		@Param("endedStatus") PromotionStatus endedStatus,
		@Param("now") LocalDateTime now
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

	@Query("""
		select pp
		from PromotionProduct pp
		join fetch pp.promotion p
		where pp.productId in :productIds
			and p.status <> :endedStatus
			and p.startAt <= :now
			and p.endAt >= :now
		""")
	List<PromotionProduct> findApplicablePromotionProductsByProductIds(
		@Param("productIds") List<Long> productIds,
		@Param("endedStatus") PromotionStatus endedStatus,
		@Param("now") LocalDateTime now
	);
}