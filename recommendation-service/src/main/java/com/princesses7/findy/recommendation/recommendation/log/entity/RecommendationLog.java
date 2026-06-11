package com.princesses7.findy.recommendation.recommendation.log.entity;

import java.math.BigDecimal;

import com.princesses7.findy.recommendation.global.entity.BaseTimeEntity;
import com.princesses7.findy.recommendation.recommendation.log.type.RecommendationLogType;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "recommendation_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationLog extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recommendation_log_id")
	private Long recommendationLogId;

	@Column(name = "source_recommendation_log_id")
	private Long sourceRecommendationLogId;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "source_product_id")
	private Long sourceProductId;

	@Column(name = "store_id")
	private Long storeId;

	@Enumerated(EnumType.STRING)
	@Column(name = "recommendation_type", nullable = false, length = 50)
	private RecommendationType recommendationType;

	@Enumerated(EnumType.STRING)
	@Column(name = "log_type", nullable = false, length = 50)
	private RecommendationLogType logType;

	@Column(name = "display_location", nullable = false, length = 100)
	private String displayLocation;

	@Column(name = "recommendation_rank")
	private Integer recommendationRank;

	@Column(name = "score", precision = 8, scale = 3)
	private BigDecimal score;

	@Column(name = "reason", length = 500)
	private String reason;

	private RecommendationLog(
		Long sourceRecommendationLogId,
		Long orderId,
		Long userId,
		Long productId,
		Long sourceProductId,
		Long storeId,
		RecommendationType recommendationType,
		RecommendationLogType logType,
		String displayLocation,
		Integer recommendationRank,
		BigDecimal score,
		String reason
	) {
		this.sourceRecommendationLogId = sourceRecommendationLogId;
		this.orderId = orderId;
		this.userId = userId;
		this.productId = productId;
		this.sourceProductId = sourceProductId;
		this.storeId = storeId;
		this.recommendationType = recommendationType;
		this.logType = logType;
		this.displayLocation = displayLocation;
		this.recommendationRank = recommendationRank;
		this.score = score;
		this.reason = reason;
	}

	public static RecommendationLog impression(
		Long userId,
		Long productId,
		Long sourceProductId,
		Long storeId,
		RecommendationType recommendationType,
		String displayLocation,
		Integer recommendationRank,
		BigDecimal score,
		String reason
	) {
		return new RecommendationLog(
			null,
			null,
			userId,
			productId,
			sourceProductId,
			storeId,
			recommendationType,
			RecommendationLogType.IMPRESSION,
			displayLocation,
			recommendationRank,
			score,
			reason
		);
	}

	public static RecommendationLog click(
		Long userId,
		Long productId,
		Long sourceProductId,
		Long storeId,
		RecommendationType recommendationType,
		String displayLocation,
		Integer recommendationRank,
		BigDecimal score
	) {
		return new RecommendationLog(
			null,
			null,
			userId,
			productId,
			sourceProductId,
			storeId,
			recommendationType,
			RecommendationLogType.CLICK,
			displayLocation,
			recommendationRank,
			score,
			null
		);
	}

	public static RecommendationLog clickFromImpression(RecommendationLog impressionLog) {
		return new RecommendationLog(
			impressionLog.getRecommendationLogId(),
			null,
			impressionLog.getUserId(),
			impressionLog.getProductId(),
			impressionLog.getSourceProductId(),
			impressionLog.getStoreId(),
			impressionLog.getRecommendationType(),
			RecommendationLogType.CLICK,
			impressionLog.getDisplayLocation(),
			impressionLog.getRecommendationRank(),
			impressionLog.getScore(),
			impressionLog.getReason()
		);
	}

	public static RecommendationLog selectionFromImpression(RecommendationLog impressionLog) {
		return new RecommendationLog(
			impressionLog.getRecommendationLogId(),
			null,
			impressionLog.getUserId(),
			impressionLog.getProductId(),
			impressionLog.getSourceProductId(),
			impressionLog.getStoreId(),
			impressionLog.getRecommendationType(),
			RecommendationLogType.SELECTION,
			impressionLog.getDisplayLocation(),
			impressionLog.getRecommendationRank(),
			impressionLog.getScore(),
			impressionLog.getReason()
		);
	}

	public static RecommendationLog purchaseFromImpression(
		RecommendationLog impressionLog,
		Long orderId
	) {
		return new RecommendationLog(
			impressionLog.getRecommendationLogId(),
			orderId,
			impressionLog.getUserId(),
			impressionLog.getProductId(),
			impressionLog.getSourceProductId(),
			impressionLog.getStoreId(),
			impressionLog.getRecommendationType(),
			RecommendationLogType.PURCHASE,
			impressionLog.getDisplayLocation(),
			impressionLog.getRecommendationRank(),
			impressionLog.getScore(),
			impressionLog.getReason()
		);
	}
}