package com.princesses7.findy.recommendation.embedding.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.embedding.entity.ProductEmbedding;

public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, Long> {

	Optional<ProductEmbedding> findByProductId(Long productId);

	List<ProductEmbedding> findByModelAndDimensions(String model, int dimensions);
}