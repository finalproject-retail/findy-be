package com.princesses7.findy.shopping.promotion.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.promotion.exception.PromotionException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "promotion_products",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_promotion_product",
			columnNames = {"promotion_id", "product_id"}
		)
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionProduct extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "promotion_product_id")
	private Long promotionProductId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "promotion_id", nullable = false)
	private Promotion promotion;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "promotion_price")
	private Integer promotionPrice;

	@Column(name = "grid_id", nullable = false)
	private Long gridId;

	private PromotionProduct(
		Promotion promotion,
		Long productId,
		Integer promotionPrice,
		Long gridId
	) {
		validatePromotionProduct(promotion, productId, promotionPrice, gridId);

		this.promotion = promotion;
		this.productId = productId;
		this.promotionPrice = promotionPrice;
		this.gridId = gridId;
	}

	public static PromotionProduct create(
		Promotion promotion,
		Long productId,
		Integer promotionPrice,
		Long gridId
	) {
		return new PromotionProduct(
			promotion,
			productId,
			promotionPrice,
			gridId
		);
	}

	public boolean belongsTo(Long promotionId) {
		return promotion.getPromotionId().equals(promotionId);
	}

	private static void validatePromotionProduct(
		Promotion promotion,
		Long productId,
		Integer promotionPrice,
		Long gridId
	) {
		if (promotion == null || productId == null || gridId == null) {
			throw new PromotionException(INVALID_PROMOTION_PRODUCT);
		}

		if (promotionPrice != null && promotionPrice < 0) {
			throw new PromotionException(INVALID_PROMOTION_PRODUCT);
		}
	}
}