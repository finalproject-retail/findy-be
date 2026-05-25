package com.princesses7.findy.recommendation.embedding.entity;

import java.util.List;

import com.princesses7.findy.recommendation.embedding.util.EmbeddingVectorConverter;
import com.princesses7.findy.recommendation.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "product_embeddings",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_product_embeddings_product_id", columnNames = "product_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEmbedding extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_embedding_id")
	private Long productEmbeddingId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "model", nullable = false)
	private String model;

	@Column(name = "dimensions", nullable = false)
	private int dimensions;

	@Column(name = "embedding", nullable = false, columnDefinition = "TEXT")
	private String embedding;

	public static ProductEmbedding create(
		Long productId,
		String model,
		int dimensions,
		List<Double> embedding
	) {
		ProductEmbedding productEmbedding = new ProductEmbedding();
		productEmbedding.productId = productId;
		productEmbedding.update(model, dimensions, embedding);

		return productEmbedding;
	}

	public void update(
		String model,
		int dimensions,
		List<Double> embedding
	) {
		this.model = model;
		this.dimensions = dimensions;
		this.embedding = EmbeddingVectorConverter.toText(embedding);
	}

	public List<Double> getEmbeddingVector() {
		return EmbeddingVectorConverter.toVector(embedding);
	}
}