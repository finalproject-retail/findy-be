package com.princesses7.findy.recommendation.embedding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "recommendation.embedding.auto-sync")
public record ProductEmbeddingAutoSyncProperties(
	boolean enabled,
	int batchSize
) {

	public ProductEmbeddingAutoSyncProperties {
		if (batchSize <= 0) {
			batchSize = 50;
		}
	}
}