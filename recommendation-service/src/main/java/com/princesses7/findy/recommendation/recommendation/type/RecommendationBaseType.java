package com.princesses7.findy.recommendation.recommendation.type;

/**
 * 개인 맞춤 추천 결과가 어떤 데이터를 기준으로 생성되었는지 나타내는 타입
 */
public enum RecommendationBaseType {

	/**
	 * 사용자의 첫 로그인 설문 선호 정보만 기반으로 추천한 경우
	 */
	PREFERENCE_ONLY,

	/**
	 * 사용자의 선호 정보와 구매 기록을 함께 반영하여 추천한 경우
	 */
	PREFERENCE_WITH_PURCHASE_HISTORY,

	/**
	 * 사용자의 선호 정보는 없고 구매 기록만 기반으로 추천한 경우
	 */
	PURCHASE_HISTORY_ONLY,

	/**
	 * 선호 정보와 구매 기록이 모두 부족하여 인기 상품을 추천한 경우
	 */
	POPULAR_FALLBACK,

	/**
	 * 선호 정보는 있지만 상품 임베딩 데이터가 없어 임베딩 기반 추천을 수행하지 못한 경우
	 */
	NO_PRODUCT_EMBEDDING_FALLBACK
}