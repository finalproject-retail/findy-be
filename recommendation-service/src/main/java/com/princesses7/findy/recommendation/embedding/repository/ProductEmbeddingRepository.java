package com.princesses7.findy.recommendation.embedding.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.embedding.entity.ProductEmbedding;

public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, Long> {

	Optional<ProductEmbedding> findByProductId(Long productId);

	List<ProductEmbedding> findByModelAndDimensions(String model, int dimensions);

	@Query("""
		SELECT e
		FROM ProductEmbedding e
		WHERE e.model = :model
			AND e.dimensions = :dimensions
			AND EXISTS (
				SELECT p.productId
				FROM ProductSnapshot p
				WHERE p.productId = e.productId
					AND p.deleted = FALSE
					AND p.saleStatus NOT IN ('SOLD_OUT', 'DISCONTINUED')
			)
			AND EXISTS (
				SELECT i.inventoryId
				FROM InventorySnapshot i
				WHERE i.productId = e.productId
					AND i.storeId = :storeId
					AND i.stockQuantity > 0
					AND i.stockStatus NOT IN ('OUT_OF_STOCK', 'SOLD_OUT')
			)
		ORDER BY e.productId ASC
		""")
	List<ProductEmbedding> findRecommendableCandidates(
		@Param("model") String model,
		@Param("dimensions") int dimensions,
		@Param("storeId") Long storeId,
		Pageable pageable
	);

	List<ProductEmbedding> findByProductIdIn(Collection<Long> productIds);
}