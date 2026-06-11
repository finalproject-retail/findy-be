package com.princesses7.findy.recommendation.embedding.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.embedding.config.ProductEmbeddingAutoSyncProperties;
import com.princesses7.findy.recommendation.embedding.dto.response.ProductEmbeddingBatchResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
	prefix = "recommendation.embedding.auto-sync",
	name = "enabled",
	havingValue = "true",
	matchIfMissing = true
)
public class ProductEmbeddingAutoSyncScheduler {

	private final ProductEmbeddingService productEmbeddingService;
	private final ProductEmbeddingAutoSyncProperties properties;

	@Scheduled(
		initialDelayString = "${recommendation.embedding.auto-sync.initial-delay-ms:30000}",
		fixedDelayString = "${recommendation.embedding.auto-sync.fixed-delay-ms:900000}"
	)
	public void syncMissingProductEmbeddings() {
		try {
			ProductEmbeddingBatchResponse response = productEmbeddingService.createMissingProductEmbeddings(
				properties.batchSize()
			);

			if (response.savedCount() > 0) {
				log.info(
					"Missing product embeddings synced. requestedCount={}, savedCount={}",
					response.requestedCount(),
					response.savedCount()
				);
			}
		} catch (Exception exception) {
			log.warn(
				"Missing product embedding auto sync failed. message={}",
				exception.getMessage()
			);
		}
	}
}