package com.princesses7.findy.analytics.recommendation.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.princesses7.findy.analytics.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "recommendation_click_rate_analytics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationClickRateAnalytics extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recommendation_click_rate_analytics_id")
	private Long id;

	@Column(name = "recommendation_type", nullable = false, length = 50)
	private String recommendationType;

	@Column(name = "product_id")
	private Long productId;

	@Column(name = "product_name")
	private String productName;

	@Column(name = "impression_count", nullable = false)
	private Long impressionCount;

	@Column(name = "click_count", nullable = false)
	private Long clickCount;

	@Column(name = "click_rate", nullable = false, precision = 6, scale = 2)
	private BigDecimal clickRate;

	@Column(name = "analysis_date", nullable = false)
	private LocalDate analysisDate;
}