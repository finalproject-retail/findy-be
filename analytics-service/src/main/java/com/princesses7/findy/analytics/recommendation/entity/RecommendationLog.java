package com.princesses7.findy.analytics.recommendation.entity;

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
@Table(name = "recommendation_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationLog extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recommendation_log_id")
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "product_name")
	private String productName;

	@Column(name = "source_product_id")
	private Long sourceProductId;

	@Column(name = "recommendation_type", nullable = false, length = 50)
	private String recommendationType;

	@Column(name = "display_position", length = 100)
	private String displayPosition;

	@Column(name = "is_clicked", nullable = false)
	private boolean clicked;

	@Column(name = "is_purchased", nullable = false)
	private boolean purchased;
}