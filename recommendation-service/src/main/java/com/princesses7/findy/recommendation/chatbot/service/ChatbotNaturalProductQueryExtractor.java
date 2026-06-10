package com.princesses7.findy.recommendation.chatbot.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotNaturalProductQuery;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ChatbotNaturalProductQueryExtractor {

	private final OpenAiChatClient openAiChatClient;
	private final ObjectMapper objectMapper;

	public ChatbotNaturalProductQuery extract(String message) {
		if (message == null || message.isBlank()) {
			return ChatbotNaturalProductQuery.fallback(message);
		}

		try {
			String response = openAiChatClient.jsonChat(List.of(
				OpenAiChatMessage.system(systemPrompt()),
				OpenAiChatMessage.user(message)
			));

			return objectMapper.readValue(
				cleanJson(response),
				ChatbotNaturalProductQuery.class
			);
		} catch (Exception exception) {
			return ChatbotNaturalProductQuery.fallback(message);
		}
	}

	private String systemPrompt() {
		return """
			너는 마트 상품 추천 검색어 추출기이다.
			
			사용자의 자연어 상품 추천 요청을 실제 DB 검색에 사용할 JSON으로만 변환한다.
			설명 문장, 마크다운, 코드블록은 포함하지 않는다.
			
			응답 형식:
			{
			  "naturalLanguageQuery": "사용자 원문",
			  "categoryKeywords": [],
			  "productKeywords": [],
			  "preferenceKeywords": []
			}
			
			추출 기준:
			- categoryKeywords에는 사용자가 말한 상품군, 식품군, 카테고리명을 넣는다.
			- productKeywords에는 사용자가 말한 구체적인 상품명 또는 상품명 일부를 넣는다.
			- preferenceKeywords에는 맛, 취향, 상황, 용도, 식감, 가격대 같은 선호 조건을 넣는다.
			- 사용자가 말하지 않은 상품명은 지어내지 않는다.
			- 특정 브랜드나 상품을 임의로 보강하지 않는다.
			- 검색 가능한 짧은 명사 또는 짧은 형용 표현 위주로 반환한다.
			- 상품군이 명확하지 않으면 categoryKeywords는 비워둔다.
			- 중복 키워드는 제거한다.
			""";
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
}