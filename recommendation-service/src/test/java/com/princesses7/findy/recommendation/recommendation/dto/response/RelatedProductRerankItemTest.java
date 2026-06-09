package com.princesses7.findy.recommendation.recommendation.dto.response;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.recommendation.recommendation.type.RelatedProductRelationType;

class RelatedProductRerankItemTest {

	@Test
	@DisplayName("COMPLEMENTARY 관계 타입이면 연관 추천 대상으로 판단한다")
	void complementaryRelationTypeIsRecommendable() {
		RelatedProductRerankItem item = new RelatedProductRerankItem(
			10001L,
			0.9,
			"COMPLEMENTARY",
			"함께 구매하기 좋은 상품입니다."
		);

		assertThat(item.safeRelationType()).isEqualTo(RelatedProductRelationType.COMPLEMENTARY);
		assertThat(item.isComplementary()).isTrue();
	}

	@Test
	@DisplayName("RECIPE_INGREDIENT 관계 타입이면 상품 상세 연관 추천 대상에서 제외한다")
	void recipeIngredientRelationTypeIsNotRecommendable() {
		RelatedProductRerankItem item = new RelatedProductRerankItem(
			11008L,
			0.8,
			"RECIPE_INGREDIENT",
			"특정 요리를 만들기 위한 재료입니다."
		);

		assertThat(item.safeRelationType()).isEqualTo(RelatedProductRelationType.RECIPE_INGREDIENT);
		assertThat(item.isComplementary()).isFalse();
	}

	@Test
	@DisplayName("알 수 없는 관계 타입은 UNRELATED로 처리한다")
	void unknownRelationTypeIsUnrelated() {
		RelatedProductRerankItem item = new RelatedProductRerankItem(
			11008L,
			0.8,
			"UNKNOWN",
			"알 수 없는 관계입니다."
		);

		assertThat(item.safeRelationType()).isEqualTo(RelatedProductRelationType.UNRELATED);
		assertThat(item.isComplementary()).isFalse();
	}

	@Test
	@DisplayName("relationScore는 0부터 1 사이로 보정한다")
	void relationScoreIsClamped() {
		RelatedProductRerankItem highScoreItem = new RelatedProductRerankItem(
			10001L,
			1.5,
			"COMPLEMENTARY",
			"함께 구매하기 좋은 상품입니다."
		);

		RelatedProductRerankItem lowScoreItem = new RelatedProductRerankItem(
			10002L,
			-0.5,
			"COMPLEMENTARY",
			"함께 구매하기 좋은 상품입니다."
		);

		assertThat(highScoreItem.safeScore()).isEqualTo(1.0);
		assertThat(lowScoreItem.safeScore()).isEqualTo(0.0);
	}
}