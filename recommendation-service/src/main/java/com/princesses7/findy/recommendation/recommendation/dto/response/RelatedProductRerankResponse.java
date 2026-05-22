package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.util.List;

public record RelatedProductRerankResponse(
	List<RelatedProductRerankItem> recommendations
) {

	public List<RelatedProductRerankItem> safeRecommendations() {
		if (recommendations == null) {
			return List.of();
		}

		return recommendations;
	}
}