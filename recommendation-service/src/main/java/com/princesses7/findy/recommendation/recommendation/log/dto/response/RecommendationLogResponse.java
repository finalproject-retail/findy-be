package com.princesses7.findy.recommendation.recommendation.log.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.recommendation.log.entity.RecommendationLog;
import com.princesses7.findy.recommendation.recommendation.log.type.RecommendationLogType;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record RecommendationLogResponse(
	Long recommendationLogId,
	Long userId,
	Long productId,
	Long sourceProductId,
	Long storeId,
	RecommendationType recommendationType,
	RecommendationLogType logType,
	String displayLocation,
	Integer recommendationRank,
	BigDecimal score,
	LocalDateTime createdAt
) {

	public static RecommendationLogResponse from(RecommendationLog recommendationLog) {
		return new RecommendationLogResponse(
			recommendationLog.getRecommendationLogId(),
			recommendationLog.getUserId(),
			recommendationLog.getProductId(),
			recommendationLog.getSourceProductId(),
			recommendationLog.getStoreId(),
			recommendationLog.getRecommendationType(),
			recommendationLog.getLogType(),
			recommendationLog.getDisplayLocation(),
			recommendationLog.getRecommendationRank(),
			recommendationLog.getScore(),
			recommendationLog.getCreatedAt()
		);
	}
}