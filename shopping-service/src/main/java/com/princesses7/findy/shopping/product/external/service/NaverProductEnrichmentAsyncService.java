package com.princesses7.findy.shopping.product.external.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import com.princesses7.findy.shopping.product.external.dto.response.NaverProductEnrichmentAsyncStatusResponse;
import com.princesses7.findy.shopping.product.external.dto.response.NaverProductEnrichmentResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverProductEnrichmentAsyncService {

	private static final String STATUS_IDLE = "IDLE";
	private static final String STATUS_RUNNING = "RUNNING";
	private static final String STATUS_COMPLETED = "COMPLETED";
	private static final String STATUS_FAILED = "FAILED";
	private static final String STATUS_ALREADY_RUNNING = "ALREADY_RUNNING";

	private final NaverProductEnrichmentService enrichmentService;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicReference<NaverProductEnrichmentAsyncStatusResponse> status = new AtomicReference<>(
		new NaverProductEnrichmentAsyncStatusResponse(
			false,
			STATUS_IDLE,
			0,
			0,
			null,
			false,
			0,
			0,
			0,
			0,
			0,
			0,
			null,
			null,
			null,
			null
		)
	);

	public NaverProductEnrichmentAsyncStatusResponse start(
		int offset,
		int limit,
		long delayMillis
	) {
		int resolvedOffset = Math.max(offset, 0);
		int resolvedLimit = limit <= 0 ? 20 : limit;
		long resolvedDelayMillis = Math.max(delayMillis, 0);

		if (!running.compareAndSet(false, true)) {
			NaverProductEnrichmentAsyncStatusResponse current = status.get();
			return updateStatus(current, STATUS_ALREADY_RUNNING, current.running(), current.errorMessage());
		}

		LocalDateTime now = LocalDateTime.now();
		status.set(new NaverProductEnrichmentAsyncStatusResponse(
			true,
			STATUS_RUNNING,
			resolvedOffset,
			resolvedLimit,
			null,
			true,
			0,
			0,
			0,
			0,
			0,
			0,
			null,
			now,
			now,
			null
		));

		CompletableFuture.runAsync(() -> run(resolvedOffset, resolvedLimit, resolvedDelayMillis));

		return status.get();
	}

	public NaverProductEnrichmentAsyncStatusResponse getStatus() {
		return status.get();
	}

	private void run(
		int startOffset,
		int limit,
		long delayMillis
	) {
		int offset = startOffset;
		NaverProductEnrichmentAsyncStatusResponse previous = status.get();

		try {
			while (running.get()) {
				NaverProductEnrichmentResponse result = enrichmentService.enrichMissingProducts(offset, limit);

				previous = accumulate(previous, result, offset, limit);
				status.set(previous);

				if (!result.hasNext() || result.nextOffset() == null) {
					status.set(updateStatus(previous, STATUS_COMPLETED, false, null));
					return;
				}

				offset = startOffset;
				sleep(delayMillis);
			}
		} catch (Exception exception) {
			log.warn("Naver product enrichment async job failed.", exception);
			status.set(updateStatus(
				previous,
				STATUS_FAILED,
				false,
				exception.getClass().getSimpleName() + ": " + exception.getMessage()
			));
		} finally {
			running.set(false);
		}
	}

	private NaverProductEnrichmentAsyncStatusResponse accumulate(
		NaverProductEnrichmentAsyncStatusResponse previous,
		NaverProductEnrichmentResponse result,
		int offset,
		int limit
	) {
		return new NaverProductEnrichmentAsyncStatusResponse(
			true,
			STATUS_RUNNING,
			offset,
			limit,
			result.nextOffset(),
			result.hasNext(),
			previous.batchCount() + 1,
			previous.processedProductCount() + result.totalCount(),
			previous.matchedCount() + result.matchedCount(),
			previous.reviewRequiredCount() + result.reviewRequiredCount(),
			previous.skippedCount() + result.skippedCount(),
			previous.appliedCount() + result.appliedCount(),
			null,
			previous.startedAt(),
			LocalDateTime.now(),
			null
		);
	}

	private NaverProductEnrichmentAsyncStatusResponse updateStatus(
		NaverProductEnrichmentAsyncStatusResponse current,
		String statusValue,
		boolean runningValue,
		String errorMessage
	) {
		LocalDateTime now = LocalDateTime.now();

		return new NaverProductEnrichmentAsyncStatusResponse(
			runningValue,
			statusValue,
			current.offset(),
			current.limit(),
			current.nextOffset(),
			current.hasNext(),
			current.batchCount(),
			current.processedProductCount(),
			current.matchedCount(),
			current.reviewRequiredCount(),
			current.skippedCount(),
			current.appliedCount(),
			errorMessage,
			current.startedAt(),
			now,
			runningValue ? null : now
		);
	}

	private void sleep(long delayMillis) {
		if (delayMillis <= 0) {
			return;
		}

		try {
			Thread.sleep(delayMillis);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Naver enrichment job interrupted", exception);
		}
	}
}
