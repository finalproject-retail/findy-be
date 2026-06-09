package com.princesses7.findy.recommendation.embedding.runner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.embedding.dto.response.ProductEmbeddingBatchResponse;
import com.princesses7.findy.recommendation.embedding.service.ProductEmbeddingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEmbeddingStartupRunner {

	private final ProductEmbeddingService productEmbeddingService;

	@Value("${recommendation.embedding.startup-sync-enabled:true}")
	private boolean startupSyncEnabled;

	@Value("${recommendation.embedding.startup-sync-limit:100}")
	private int startupSyncLimit;

	@EventListener(ApplicationReadyEvent.class)
	public void createMissingEmbeddingsOnStartup() {
		if (!startupSyncEnabled) {
			log.info("서버 시작 시 상품 임베딩 자동 생성이 비활성화되어 있습니다.");
			return;
		}

		try {
			ProductEmbeddingBatchResponse response =
				productEmbeddingService.createMissingProductEmbeddings(startupSyncLimit);

			log.info(
				"서버 시작 후 누락 상품 임베딩 생성 완료 requestedCount={}, savedCount={}",
				response.requestedCount(),
				response.savedCount()
			);
		} catch (Exception e) {
			log.warn("서버 시작 후 누락 상품 임베딩 생성 실패", e);
		}
	}
}