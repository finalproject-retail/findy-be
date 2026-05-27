package com.princesses7.findy.recommendation.recommendation.log.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationClickLogRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.response.RecommendationLogResponse;
import com.princesses7.findy.recommendation.recommendation.log.dto.service.RecommendationImpressionLogCommand;
import com.princesses7.findy.recommendation.recommendation.log.entity.RecommendationLog;
import com.princesses7.findy.recommendation.recommendation.log.repository.RecommendationLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationLogService {

	private final RecommendationLogRepository recommendationLogRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveImpressionLogs(RecommendationImpressionLogCommand command) {
		if (command.recommendations() == null || command.recommendations().isEmpty()) {
			return;
		}

		List<RecommendationLog> logs = IntStream.range(0, command.recommendations().size())
			.mapToObj(index -> toImpressionLog(command, command.recommendations().get(index), index + 1))
			.toList();

		recommendationLogRepository.saveAll(logs);
	}

	@Transactional
	public RecommendationLogResponse saveClickLog(RecommendationClickLogRequest request) {
		RecommendationLog log = RecommendationLog.click(
			request.userId(),
			request.productId(),
			request.sourceProductId(),
			request.storeId(),
			request.recommendationType(),
			request.displayLocation(),
			request.recommendationRank(),
			request.score()
		);

		RecommendationLog savedLog = recommendationLogRepository.save(log);

		return RecommendationLogResponse.from(savedLog);
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
}