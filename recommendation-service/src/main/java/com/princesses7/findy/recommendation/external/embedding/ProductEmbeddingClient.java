package com.princesses7.findy.recommendation.external.embedding;

import java.util.List;

public interface ProductEmbeddingClient {

	List<Double> createEmbedding(String text);

	String model();

	int dimensions();
}
