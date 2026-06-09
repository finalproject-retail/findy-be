package com.princesses7.findy.recommendation.recommendation.dto.response;

import com.princesses7.findy.recommendation.recommendation.type.RelatedProductRelationType;

public record RelatedProductRerankItem(
	Long productId,
	Double relationScore,
	String relationType,
	String reason
) {

	public RelatedProductRelationType safeRelationType() {
		return RelatedProductRelationType.from(relationType);
	}

	public boolean isComplementary() {
		return safeRelationType() == RelatedProductRelationType.COMPLEMENTARY;
	}

	public double safeScore() {
		if (relationScore == null) {
			return 0.0;
		}

		return Math.max(0.0, Math.min(relationScore, 1.0));
	}

	public String safeReason() {
		if (reason == null || reason.isBlank()) {
			return "현재 상품과 함께 구매하기 좋은 보완 상품입니다.";
		}

		return reason;
	}
}