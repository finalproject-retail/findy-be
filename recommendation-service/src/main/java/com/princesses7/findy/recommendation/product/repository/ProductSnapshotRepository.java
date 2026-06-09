package com.princesses7.findy.recommendation.product.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;

public interface ProductSnapshotRepository extends JpaRepository<ProductSnapshot, Long> {

	List<ProductSnapshot> findByDeletedFalse(Pageable pageable);

	List<ProductSnapshot> findByProductIdIn(Collection<Long> productIds);

	List<ProductSnapshot> findByProductIdInAndDeletedFalse(Collection<Long> productIds);

	List<ProductSnapshot> findByCategoryIdAndDeletedFalse(Long categoryId);

	List<ProductSnapshot> findByCategoryIdInAndDeletedFalse(
		Collection<Long> categoryIds,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM ProductSnapshot p
		WHERE p.deleted = FALSE
			AND (
				LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(COALESCE(p.brandName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
		ORDER BY p.discountRate DESC, p.salePrice ASC
		""")
	List<ProductSnapshot> searchByKeyword(
		@Param("keyword") String keyword,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM ProductSnapshot p
		WHERE p.deleted = FALSE
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
	List<ProductSnapshot> findMissingEmbeddingProducts(
		@Param("model") String model,
		@Param("dimensions") int dimensions,
		Pageable pageable
	);
}