package com.princesses7.findy.recommendation.chatbot.service;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotCouponContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotProductContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotPromotionContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotShoppingContextResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;

class ChatbotShoppingContextPromptBuilderTest {

	private final ChatbotShoppingContextPromptBuilder builder = new ChatbotShoppingContextPromptBuilder();

	@Test
	@DisplayName("쇼핑 데이터가 있으면 상품 재고 행사 쿠폰 정보를 프롬프트로 구성한다")
	void buildShoppingContextPrompt() {
		ChatbotShoppingContextResponse context = new ChatbotShoppingContextResponse(
			1L,
			"사리곰탕",
			ChatIntent.INVENTORY_INQUIRY,
			List.of(
				new ChatbotProductContextResponse(
					10002L,
					"농심",
					"시드_사리곰탕면",
					1L,
					"라면/면류",
					6000,
					5400,
					BigDecimal.valueOf(10.00),
					"ON_SALE",
					3,
					"LOW_STOCK",
					"품절임박 3개",
					List.of(
						new ChatbotPromotionContextResponse(
							20001L,
							"라면 기획 할인 행사",
							"DISCOUNT",
							4900,
							"15% 할인"
						)
					),
					List.of(
						new ChatbotCouponContextResponse(
							20001L,
							"라면 카테고리 1,000원 할인 쿠폰",
							"PRODUCT",
							"AMOUNT",
							1000,
							0,
							"1000원 할인"
						)
					)
				)
			)
		);

		String prompt = builder.build(context);

		assertThat(prompt).contains("시드_사리곰탕면");
		assertThat(prompt).contains("품절임박 3개");
		assertThat(prompt).contains("라면 기획 할인 행사");
		assertThat(prompt).contains("라면 카테고리 1,000원 할인 쿠폰");
	}

	@Test
	@DisplayName("쇼핑 데이터가 없으면 조회 결과 없음 안내 프롬프트를 구성한다")
	void buildEmptyShoppingContextPrompt() {
		ChatbotShoppingContextResponse context = ChatbotShoppingContextResponse.empty(
			1L,
			"없는상품",
			ChatIntent.PRODUCT_SEARCH
		);

		String prompt = builder.build(context);

		assertThat(prompt).contains("데이터를 찾지 못했다");
		assertThat(prompt).contains("지어내지 않는다");
	}
}