package com.princesses7.findy.recommendation.promotion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(schema = "shopping_service", name = "promotion_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionProductSnapshot {

	@Id
	@Column(name = "promotion_product_id")
	private Long promotionProductId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "promotion_id", nullable = false)
	private PromotionSnapshot promotion;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "promotion_price")
	private Integer promotionPrice;

	@Column(name = "grid_id", nullable = false)
	private Long gridId;
}
