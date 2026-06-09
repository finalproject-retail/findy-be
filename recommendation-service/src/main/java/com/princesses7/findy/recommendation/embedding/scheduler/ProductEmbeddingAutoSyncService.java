package com.princesses7.findy.recommendation.embedding.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.princesses7.findy.recommendation.embedding.dto.response.ProductEmbeddingBatchResponse;
import com.princesses7.findy.recommendation.embedding.service.ProductEmbeddingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEmbeddingAutoSyncService {

	private final ProductEmbeddingService productEmbeddingService;

	@Value("${recommendation.embedding.scheduled-sync-enabled:false}")
	private boolean scheduledSyncEnabled;

	@Value("${recommendation.embedding.sync-limit:100}")
	private int syncLimit;

	@Scheduled(cron = "${recommendation.embedding.cron:0 0 3 * * *}")
	public void syncMissingProductEmbeddings() {
		if (!scheduledSyncEnabled) {
			return;
		}

		try {
			ProductEmbeddingBatchResponse response =
				productEmbeddingService.createMissingProductEmbeddings(syncLimit);

			if (response.savedCount() > 0) {
				log.info(
					"상품 임베딩 자동 생성 완료 requestedCount={}, savedCount={}",
					response.requestedCount(),
					response.savedCount()
				);
			}
		} catch (Exception e) {
			log.warn("상품 임베딩 자동 생성 실패", e);
		}
	}
}