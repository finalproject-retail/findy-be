package com.princesses7.findy.recommendation.product.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;

public interface ProductSnapshotRepository extends JpaRepository<ProductSnapshot, Long> {

	List<ProductSnapshot> findByDeletedAtIsNull(Pageable pageable);

	List<ProductSnapshot> findByProductIdIn(Collection<Long> productIds);

	List<ProductSnapshot> findByProductIdInAndDeletedAtIsNull(Collection<Long> productIds);

	List<ProductSnapshot> findByCategoryIdAndDeletedAtIsNull(Long categoryId);

	List<ProductSnapshot> findByCategoryIdInAndDeletedAtIsNull(
		Collection<Long> categoryIds,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM ProductSnapshot p
		WHERE p.deletedAt IS NULL
			AND p.productId <> :sourceProductId
			AND p.categoryId = :categoryId
			AND p.saleStatus NOT IN ('SOLD_OUT', 'DISCONTINUED')
		ORDER BY p.originalPrice ASC, p.productId ASC
		""")
	List<ProductSnapshot> findFallbackRelatedProductsByCategory(
		@Param("sourceProductId") Long sourceProductId,
		@Param("categoryId") Long categoryId,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM ProductSnapshot p
		WHERE p.deletedAt IS NULL
			AND p.productId <> :sourceProductId
			AND p.saleStatus NOT IN ('SOLD_OUT', 'DISCONTINUED')
		ORDER BY p.originalPrice ASC, p.productId ASC
		""")
	List<ProductSnapshot> findFallbackRelatedProducts(
		@Param("sourceProductId") Long sourceProductId,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM ProductSnapshot p
		WHERE p.deletedAt IS NULL
			AND p.saleStatus NOT IN ('SOLD_OUT', 'DISCONTINUED')
			AND NOT EXISTS (
				SELECT e
				FROM ProductEmbedding e
				WHERE e.productId = p.productId
					AND e.model = :model
					AND e.dimensions = :dimensions
			)
		ORDER BY p.productId ASC
		""")
	List<ProductSnapshot> findEmbeddingAutoSyncTargets(
		@Param("model") String model,
		@Param("dimensions") int dimensions,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM ProductSnapshot p
		WHERE p.deletedAt IS NULL
			AND (
				LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(COALESCE(p.brandName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
		ORDER BY p.originalPrice ASC, p.productId ASC
		""")
	List<ProductSnapshot> searchByKeyword(
		@Param("keyword") String keyword,
		Pageable pageable
	);
}