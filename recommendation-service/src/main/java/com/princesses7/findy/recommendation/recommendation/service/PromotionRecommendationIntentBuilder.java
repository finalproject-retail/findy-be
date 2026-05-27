package com.princesses7.findy.recommendation.recommendation.service;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.preference.dto.response.UserPreferenceResponse;

@Component
public class PromotionRecommendationIntentBuilder {

	public String build(UserPreferenceResponse userPreference) {
		return """
			오프라인 대형마트의 행사 상품 추천 요청입니다.
			
			사용자 선호 정보:
			- 선호 카테고리: %s
			- 쇼핑 스타일: %s
			
			추천 목표:
			현재 매장에서 진행 중인 할인, 묶음 행사, 사은품 행사 상품 중
			사용자의 선호 카테고리와 쇼핑 스타일에 의미적으로 잘 맞는 상품을 추천합니다.
			
			중요 기준:
			- 단순히 할인율이 높은 상품만 고르지 않습니다.
			- 사용자의 장보기 성향과 상품 특성이 잘 맞는지 우선 고려합니다.
			- 행사 혜택은 사용자의 선호와 관련이 있을 때 더 중요하게 반영합니다.
			- 재고가 있는 상품만 추천 후보가 됩니다.
			""".formatted(
			String.join(", ", userPreference.preferredCategories()),
			String.join(", ", userPreference.shoppingStyles())
		);
	}
}