package com.princesses7.findy.recommendation.recommendation.log.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.PromotionProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationClickLogRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationImpressionLogRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationPurchaseConversionRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationSelectionLogRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.response.RecommendationLogResponse;
import com.princesses7.findy.recommendation.recommendation.log.dto.response.RecommendationPurchaseConversionResponse;
import com.princesses7.findy.recommendation.recommendation.log.dto.service.PromotionRecommendationImpressionLogCommand;
import com.princesses7.findy.recommendation.recommendation.log.dto.service.RecommendationImpressionLogCommand;
import com.princesses7.findy.recommendation.recommendation.log.dto.service.RecommendationSingleImpressionLogCommand;
import com.princesses7.findy.recommendation.recommendation.log.entity.RecommendationLog;
import com.princesses7.findy.recommendation.recommendation.log.repository.RecommendationLogRepository;
import com.princesses7.findy.recommendation.recommendation.log.type.RecommendationLogType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationLogService {

	private static final Duration DUPLICATE_SAVE_INTERVAL = Duration.ofSeconds(3);

	private final RecommendationLogRepository recommendationLogRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<ProductRecommendationResponse> saveImpressionLogsAndAttachIds(
		RecommendationImpressionLogCommand command
	) {
		if (command.recommendations() == null || command.recommendations().isEmpty()) {
			return List.of();
		}

		List<RecommendationLog> logs = IntStream.range(0, command.recommendations().size())
			.mapToObj(index -> toImpressionLog(command, command.recommendations().get(index), index + 1))
			.toList();

		List<RecommendationLog> savedLogs = recommendationLogRepository.saveAll(logs);

		return IntStream.range(0, command.recommendations().size())
			.mapToObj(index -> command.recommendations().get(index)
				.withRecommendationLogId(savedLogs.get(index).getRecommendationLogId()))
			.toList();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<PromotionProductRecommendationResponse> savePromotionImpressionLogsAndAttachIds(
		PromotionRecommendationImpressionLogCommand command
	) {
		if (command.recommendations() == null || command.recommendations().isEmpty()) {
			return List.of();
		}

		List<RecommendationLog> logs = IntStream.range(0, command.recommendations().size())
			.mapToObj(index -> toPromotionImpressionLog(command, command.recommendations().get(index), index + 1))
			.toList();

		List<RecommendationLog> savedLogs = recommendationLogRepository.saveAll(logs);

		return IntStream.range(0, command.recommendations().size())
			.mapToObj(index -> command.recommendations().get(index)
				.withRecommendationLogId(savedLogs.get(index).getRecommendationLogId()))
			.toList();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveImpressionLogs(RecommendationImpressionLogCommand command) {
		saveImpressionLogsAndAttachIds(command);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void savePromotionImpressionLogs(PromotionRecommendationImpressionLogCommand command) {
		savePromotionImpressionLogsAndAttachIds(command);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public RecommendationLogResponse saveSingleImpressionLog(RecommendationSingleImpressionLogCommand command) {
		RecommendationLog log = RecommendationLog.impression(
			command.userId(),
			command.productId(),
			command.sourceProductId(),
			command.storeId(),
			command.recommendationType(),
			command.displayLocation(),
			command.recommendationRank(),
			command.score(),
			command.reason()
		);

		RecommendationLog savedLog = recommendationLogRepository.save(log);

		return RecommendationLogResponse.from(savedLog);
	}

	@Transactional
	public RecommendationLogResponse saveClickLog(RecommendationClickLogRequest request) {
		if (request.recommendationLogId() != null) {
			RecommendationLog impressionLog = findUsableImpressionLog(
				request.recommendationLogId(),
				request.userId(),
				request.productId()
			).orElseThrow(() -> new IllegalArgumentException("클릭 처리할 추천 노출 로그를 찾을 수 없습니다."));

			RecommendationLog clickLog = findRecentEventLog(
				impressionLog.getRecommendationLogId(),
				RecommendationLogType.CLICK
			).orElseGet(() -> recommendationLogRepository.save(
				RecommendationLog.clickFromImpression(impressionLog)
			));

			return RecommendationLogResponse.from(clickLog);
		}

		validateRawClickRequest(request);

		RecommendationLog savedLog = recommendationLogRepository.save(
			RecommendationLog.click(
				request.userId(),
				request.productId(),
				request.sourceProductId(),
				request.storeId(),
				request.recommendationType(),
				request.displayLocation(),
				request.recommendationRank(),
				request.score()
			)
		);

		return RecommendationLogResponse.from(savedLog);
	}

	@Transactional
	public RecommendationLogResponse saveSelectionLog(RecommendationSelectionLogRequest request) {
		RecommendationLog impressionLog = findUsableImpressionLog(
			request.recommendationLogId(),
			request.userId(),
			request.selectedProductId()
		).orElseThrow(() -> new IllegalArgumentException("선택 처리할 추천 노출 로그를 찾을 수 없습니다."));

		RecommendationLog selectionLog = findRecentEventLog(
			impressionLog.getRecommendationLogId(),
			RecommendationLogType.SELECTION
		).orElseGet(() -> recommendationLogRepository.save(
			RecommendationLog.selectionFromImpression(impressionLog)
		));

		return RecommendationLogResponse.from(selectionLog);
	}

	@Transactional
	public RecommendationPurchaseConversionResponse savePurchaseConversionLog(
		RecommendationPurchaseConversionRequest request
	) {
		Set<Long> purchasedProductIds = new HashSet<>(request.purchasedProductIds());

		List<RecommendationLog> impressionLogs = recommendationLogRepository.findByRecommendationLogIdInAndLogType(
			request.recommendationLogIds(),
			RecommendationLogType.IMPRESSION
		);

		List<RecommendationLog> purchaseLogs = impressionLogs.stream()
			.filter(log -> request.userId().equals(log.getUserId()))
			.filter(log -> purchasedProductIds.contains(log.getProductId()))
			.filter(log -> !recommendationLogRepository.existsBySourceRecommendationLogIdAndLogTypeAndOrderId(
				log.getRecommendationLogId(),
				RecommendationLogType.PURCHASE,
				request.orderId()
			))
			.map(log -> RecommendationLog.purchaseFromImpression(log, request.orderId()))
			.toList();

		recommendationLogRepository.saveAll(purchaseLogs);

		return new RecommendationPurchaseConversionResponse(
			request.userId(),
			request.orderId(),
			purchaseLogs.size()
		);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public RecommendationLogResponse saveImpressionLog(RecommendationImpressionLogRequest request) {
		RecommendationLog log = RecommendationLog.impression(
			request.userId(),
			request.productId(),
			request.sourceProductId(),
			request.storeId(),
			request.recommendationType(),
			request.displayLocation(),
			request.recommendationRank(),
			request.score(),
			request.reason()
		);

		RecommendationLog savedLog = recommendationLogRepository.save(log);

		return RecommendationLogResponse.from(savedLog);
	}

	private java.util.Optional<RecommendationLog> findUsableImpressionLog(
		Long recommendationLogId,
		Long userId,
		Long productId
	) {
		if (recommendationLogId == null) {
			return java.util.Optional.empty();
		}

		return recommendationLogRepository.findById(recommendationLogId)
			.filter(log -> RecommendationLogType.IMPRESSION == log.getLogType())
			.filter(log -> userId == null || userId.equals(log.getUserId()))
			.filter(log -> productId == null || productId.equals(log.getProductId()));
	}

	private RecommendationLog toImpressionLog(
		RecommendationImpressionLogCommand command,
		ProductRecommendationResponse recommendation,
		int rank
	) {
		return RecommendationLog.impression(
			command.userId(),
			recommendation.productId(),
			command.sourceProductId(),
			command.storeId(),
			command.recommendationType(),
			command.displayLocation(),
			rank,
			BigDecimal.valueOf(recommendation.score()),
			recommendation.reason()
		);
	}

	private RecommendationLog toPromotionImpressionLog(
		PromotionRecommendationImpressionLogCommand command,
		PromotionProductRecommendationResponse recommendation,
		int rank
	) {
		return RecommendationLog.impression(
			command.userId(),
			recommendation.productId(),
			command.sourceProductId(),
			command.storeId(),
			command.recommendationType(),
			command.displayLocation(),
			rank,
			BigDecimal.valueOf(recommendation.score()),
			recommendation.reason()
		);
	}

	private Optional<RecommendationLog> findRecentEventLog(
		Long sourceRecommendationLogId,
		RecommendationLogType logType
	) {
		LocalDateTime createdAtAfter = LocalDateTime.now().minus(DUPLICATE_SAVE_INTERVAL);

		return recommendationLogRepository.findFirstBySourceRecommendationLogIdAndLogTypeAndCreatedAtAfterOrderByCreatedAtDesc(
			sourceRecommendationLogId,
			logType,
			createdAtAfter
		);
	}

	private void validateRawClickRequest(RecommendationClickLogRequest request) {
		if (request.userId() == null
			|| request.productId() == null
			|| request.recommendationType() == null
			|| request.displayLocation() == null
			|| request.displayLocation().isBlank()) {
			throw new IllegalArgumentException(
				"recommendationLogId가 없으면 userId, productId, recommendationType, displayLocation은 필수입니다."
			);
		}
	}
}