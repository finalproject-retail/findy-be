package com.princesses7.findy.shopping.product.external.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import com.princesses7.findy.shopping.product.external.dto.response.KcaMissingPriceAsyncStatusResponse;
import com.princesses7.findy.shopping.product.external.dto.response.KcaMissingPriceSyncResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KcaMissingPriceAsyncService {

	private static final String STATUS_IDLE = "IDLE";
	private static final String STATUS_RUNNING = "RUNNING";
	private static final String STATUS_COMPLETED = "COMPLETED";
	private static final String STATUS_FAILED = "FAILED";
	private static final String STATUS_ALREADY_RUNNING = "ALREADY_RUNNING";

	private final KcaMissingPriceSyncService syncService;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicReference<KcaMissingPriceAsyncStatusResponse> status = new AtomicReference<>(
		new KcaMissingPriceAsyncStatusResponse(
			false,
			STATUS_IDLE,
			null,
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
			0,
			0,
			0,
			null,
			null,
			null,
			null,
			null,
			null
		)
	);

	public KcaMissingPriceAsyncStatusResponse start(
		String goodInspectDay,
		int offset,
		int limit,
		long delayMillis
	) {
		int resolvedOffset = Math.max(offset, 0);
		int resolvedLimit = limit <= 0 ? 20 : limit;
		long resolvedDelayMillis = Math.max(delayMillis, 0);

		if (!running.compareAndSet(false, true)) {
			KcaMissingPriceAsyncStatusResponse current = status.get();
			return updateStatus(current, STATUS_ALREADY_RUNNING, current.running(), current.errorMessage());
		}

		LocalDateTime now = LocalDateTime.now();
		status.set(new KcaMissingPriceAsyncStatusResponse(
			true,
			STATUS_RUNNING,
			goodInspectDay,
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
			0,
			0,
			0,
			null,
			null,
			null,
			now,
			now,
			null
		));

		CompletableFuture.runAsync(() -> run(goodInspectDay, resolvedOffset, resolvedLimit, resolvedDelayMillis));

		return status.get();
	}

	public KcaMissingPriceAsyncStatusResponse getStatus() {
		return status.get();
	}

	private void run(
		String goodInspectDay,
		int startOffset,
		int limit,
		long delayMillis
	) {
		int offset = startOffset;
		KcaMissingPriceAsyncStatusResponse previous = status.get();

		try {
			while (running.get()) {
				KcaMissingPriceSyncResponse result = syncService.syncMissingPrices(
					goodInspectDay,
					null,
					null,
					offset,
					limit,
					null
				);

				previous = accumulate(previous, result, offset, limit);
				status.set(previous);

				if (isFailed(result)) {
					status.set(updateStatus(previous, STATUS_FAILED, false, result.kcaResultMessage()));
					return;
				}

				if (!result.hasNext() || result.nextOffset() == null) {
					status.set(updateStatus(previous, STATUS_COMPLETED, false, null));
					return;
				}

				offset = result.nextOffset();
				sleep(delayMillis);
			}
		} catch (Exception exception) {
			log.warn("KCA missing price async job failed.", exception);
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

	private KcaMissingPriceAsyncStatusResponse accumulate(
		KcaMissingPriceAsyncStatusResponse previous,
		KcaMissingPriceSyncResponse result,
		int offset,
		int limit
	) {
		return new KcaMissingPriceAsyncStatusResponse(
			true,
			STATUS_RUNNING,
			result.goodInspectDay() == null ? previous.goodInspectDay() : result.goodInspectDay(),
			offset,
			limit,
			result.nextOffset(),
			result.hasNext(),
			previous.batchCount() + 1,
			result.productInfoCount(),
			previous.processedProductCount() + result.processedProductCount(),
			previous.sourceItemCount() + result.sourceItemCount(),
			previous.aggregatedItemCount() + result.aggregatedItemCount(),
			previous.matchedCount() + result.matchedCount(),
			previous.reviewRequiredCount() + result.reviewRequiredCount(),
			previous.skippedCount() + result.skippedCount(),
			previous.appliedCount() + result.appliedCount(),
			result.kcaResultCode(),
			result.kcaResultMessage(),
			null,
			previous.startedAt(),
			LocalDateTime.now(),
			null
		);
	}

	private KcaMissingPriceAsyncStatusResponse updateStatus(
		KcaMissingPriceAsyncStatusResponse current,
		String statusValue,
		boolean runningValue,
		String errorMessage
	) {
		LocalDateTime now = LocalDateTime.now();

		return new KcaMissingPriceAsyncStatusResponse(
			runningValue,
			statusValue,
			current.goodInspectDay(),
			current.offset(),
			current.limit(),
			current.nextOffset(),
			current.hasNext(),
			current.batchCount(),
			current.productInfoCount(),
			current.processedProductCount(),
			current.sourceItemCount(),
			current.aggregatedItemCount(),
			current.matchedCount(),
			current.reviewRequiredCount(),
			current.skippedCount(),
			current.appliedCount(),
			current.kcaResultCode(),
			current.kcaResultMessage(),
			errorMessage,
			current.startedAt(),
			now,
			runningValue ? null : now
		);
	}

	private boolean isFailed(KcaMissingPriceSyncResponse result) {
		return "SYNC_FAILED".equals(result.kcaResultCode());
	}

	private void sleep(long delayMillis) {
		if (delayMillis <= 0) {
			return;
		}

		try {
			Thread.sleep(delayMillis);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("KCA async job interrupted", exception);
		}
	}
}
