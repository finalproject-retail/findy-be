package com.princesses7.findy.recommendation.chatbot.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ChatbotIntentAnalyzer {

	private final OpenAiChatClient openAiChatClient;
	private final ObjectMapper objectMapper;

	public ChatbotIntentAnalysis analyze(String message) {
		if (message == null || message.isBlank()) {
			return ChatbotIntentAnalysis.general();
		}

		try {
			String response = openAiChatClient.jsonChat(List.of(
				OpenAiChatMessage.system(systemPrompt()),
				OpenAiChatMessage.user(message)
			));

			return parse(response);
		} catch (Exception exception) {
			return ChatbotIntentAnalysis.general();
		}
	}

	private ChatbotIntentAnalysis parse(String response) throws Exception {
		String json = cleanJson(response);
		LlmIntentAnalysisResult result = objectMapper.readValue(
			json,
			LlmIntentAnalysisResult.class
		);

		return new ChatbotIntentAnalysis(
			resolveIntent(result.intent()),
			normalizeKeyword(result.keyword()),
			result.shoppingDataRequired()
		);
	}

	private ChatIntent resolveIntent(String intent) {
		if (intent == null || intent.isBlank()) {
			return ChatIntent.GENERAL;
		}

		try {
			return ChatIntent.valueOf(intent.trim());
		} catch (IllegalArgumentException exception) {
			return ChatIntent.GENERAL;
		}
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}

		return keyword.trim();
	}

	private String cleanJson(String response) {
		if (response == null || response.isBlank()) {
			return "{}";
		}

		return response
			.replace("```json", "")
			.replace("```", "")
			.trim();
	}

	private String systemPrompt() {
		return """
			너는 Findy AI 쇼핑 챗봇의 의도 분석기이다.
			
			사용자의 메시지를 분석해서 JSON 객체로만 답변한다.
			설명 문장, 마크다운, 코드블록은 포함하지 않는다.
			
			응답 형식:
			{
			  "intent": "GENERAL",
			  "keyword": null,
			  "shoppingDataRequired": false
			}
			
			intent는 반드시 아래 값 중 하나만 사용한다.
			- GENERAL
			- RECIPE_INGREDIENT_RECOMMENDATION
			- PRODUCT_SEARCH
			- INVENTORY_INQUIRY
			- PROMOTION_INQUIRY
			- COUPON_INQUIRY
			
			분석 기준:
			- 상품 존재 여부, 상품 추천, 상품 검색을 요청하면 PRODUCT_SEARCH
			- 재고, 품절, 남은 수량을 물어보면 INVENTORY_INQUIRY
			- 할인, 행사, 프로모션, 특가를 물어보면 PROMOTION_INQUIRY
			- 쿠폰을 물어보면 COUPON_INQUIRY
			- 특정 요리를 만들고 싶다고 하거나 요리 재료를 요청하면 RECIPE_INGREDIENT_RECOMMENDATION
			- 단순 인사, 일반 대화는 GENERAL
			
			keyword 기준:
			- 상품명, 카테고리명, 요리명 등 핵심 검색어만 추출한다.
			- 핵심 검색어가 없으면 null로 둔다.
			- 문장 전체를 keyword에 넣지 않는다.
			
			shoppingDataRequired 기준:
			- 상품, 재고, 행사, 쿠폰, 요리 재료와 관련된 질문이면 true
			- 일반 대화면 false
			""";
	}

	private record LlmIntentAnalysisResult(
		String intent,
		String keyword,
		boolean shoppingDataRequired
	) {
	}
}