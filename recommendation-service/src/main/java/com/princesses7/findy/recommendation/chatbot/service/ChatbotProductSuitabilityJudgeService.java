package com.princesses7.findy.recommendation.chatbot.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotNaturalProductQuery;
import com.princesses7.findy.recommendation.chatbot.dto.ChatbotProductSuitabilityJudgeItem;
import com.princesses7.findy.recommendation.chatbot.dto.ChatbotProductSuitabilityJudgeResponse;
import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ChatbotProductSuitabilityJudgeService {

	private static final double MIN_CONFIDENCE = 0.55;
	private static final int MAX_CANDIDATES_IN_PROMPT = 40;
	private static final int MAX_DESCRIPTION_LENGTH = 90;

	private final OpenAiChatClient openAiChatClient;
	private final ObjectMapper objectMapper;

	public List<ChatbotShoppingProduct> judge(
		String userMessage,
		ChatbotNaturalProductQuery naturalQuery,
		List<ChatbotShoppingProduct> candidates,
		int limit
	) {
		if (candidates == null || candidates.isEmpty()) {
			return List.of();
		}

		try {
			String response = openAiChatClient.jsonChat(List.of(
				OpenAiChatMessage.system(systemPrompt()),
				OpenAiChatMessage.user(userPrompt(
					userMessage,
					naturalQuery,
					candidates,
					limit
				))
			));

			ChatbotProductSuitabilityJudgeResponse judgeResponse = objectMapper.readValue(
				cleanJson(response),
				ChatbotProductSuitabilityJudgeResponse.class
			);

			return selectSuitableProducts(
				judgeResponse.safeItems(),
				candidates,
				limit
			);
		} catch (Exception exception) {
			return List.of();
		}
	}

	private List<ChatbotShoppingProduct> selectSuitableProducts(
		List<ChatbotProductSuitabilityJudgeItem> judgedItems,
		List<ChatbotShoppingProduct> candidates,
		int limit
	) {
		if (judgedItems == null || judgedItems.isEmpty()) {
			return List.of();
		}

		Map<Long, ChatbotShoppingProduct> candidateMap = candidates.stream()
			.collect(Collectors.toMap(
				ChatbotShoppingProduct::productId,
				candidate -> candidate,
				(left, right) -> left,
				LinkedHashMap::new
			));

		LinkedHashMap<Long, ChatbotShoppingProduct> selectedProductMap = new LinkedHashMap<>();

		judgedItems.stream()
			.filter(ChatbotProductSuitabilityJudgeItem::isSuitable)
			.filter(item -> item.safeConfidence() >= MIN_CONFIDENCE)
			.filter(item -> item.productId() != null)
			.filter(item -> candidateMap.containsKey(item.productId()))
			.sorted(Comparator.comparing(ChatbotProductSuitabilityJudgeItem::safeConfidence).reversed())
			.forEach(item -> selectedProductMap.putIfAbsent(
				item.productId(),
				candidateMap.get(item.productId())
			));

		return selectedProductMap.values()
			.stream()
			.limit(limit)
			.toList();
	}

	private String systemPrompt() {
		return """
			너는 대형마트 챗봇의 AI 상품 추천 적합도 판단 모델이다.
			
			목표:
			- 사용자 요청과 후보 상품 정보를 비교해서 실제로 추천하기 적합한 상품만 골라낸다.
			- 상품명 키워드 포함 여부만으로 판단하지 않는다.
			- 상품명, 브랜드, 카테고리, 설명, 뱃지, 가격, 재고 상태를 종합해서 판단한다.
			
			판단 예시:
			- "빈츠 같은 과자"는 초코/비스킷/쿠키/달달한 간식 계열이 적합하다.
			- "빈츠 같은 과자"에 감자칩, 새우깡, 양파링처럼 짭짤한 스낵은 부적합하다.
			- "짭짤한 과자"는 감자칩/새우깡/나초 등 짠맛 스낵이 적합하다.
			- "라면 추천"은 라면/봉지라면/컵라면 계열이 적합하다.
			
			중요 규칙:
			- 후보 목록에 있는 productId만 응답한다.
			- 후보에 없는 상품명이나 productId를 만들지 않는다.
			- suitable=true 상품을 추천 우선순위 높은 순서로 앞에 둔다.
			- confidence는 0.0 이상 1.0 이하로 준다.
			- 사용자의 취향과 반대되는 상품은 suitable=false로 둔다.
			- 반드시 JSON 객체 하나만 반환한다.
			- 마크다운 코드블록, 설명 문장, 주석을 포함하지 않는다.
			
			응답 형식:
			{
			  "items": [
			    {
			      "productId": 123,
			      "suitable": true,
			      "confidence": 0.92,
			      "reason": "판단 이유"
			    }
			  ]
			}
			""";
	}

	private String userPrompt(
		String userMessage,
		ChatbotNaturalProductQuery naturalQuery,
		List<ChatbotShoppingProduct> candidates,
		int limit
	) {
		return """
			사용자 요청:
			%s
			
			AI가 추출한 자연어 조건:
			- categoryKeywords: %s
			- productKeywords: %s
			- preferenceKeywords: %s
			
			후보 상품:
			%s
			
			요청:
			후보 상품 중 사용자 요청에 실제로 추천하기 적합한 상품을 판단해줘.
			suitable=true 상품만 최대 %d개가 최종 추천에 사용된다.
			""".formatted(
			nullToEmpty(userMessage),
			naturalQuery == null ? List.of() : naturalQuery.categoryKeywords(),
			naturalQuery == null ? List.of() : naturalQuery.productKeywords(),
			naturalQuery == null ? List.of() : naturalQuery.preferenceKeywords(),
			createCandidateText(candidates),
			limit
		);
	}

	private String createCandidateText(List<ChatbotShoppingProduct> candidates) {
		StringBuilder builder = new StringBuilder();

		candidates.stream()
			.limit(MAX_CANDIDATES_IN_PROMPT)
			.forEach(candidate -> builder.append("- productId: ")
				.append(candidate.productId())
				.append(", productName: ")
				.append(nullToEmpty(candidate.productName()))
				.append(", brandName: ")
				.append(nullToEmpty(candidate.brandName()))
				.append(", categoryName: ")
				.append(nullToEmpty(candidate.categoryName()))
				.append(", description: ")
				.append(truncate(candidate.description(), MAX_DESCRIPTION_LENGTH))
				.append(", badgeText: ")
				.append(nullToEmpty(candidate.badgeText()))
				.append(", price: ")
				.append(candidate.originalPrice())
				.append(", stockStatus: ")
				.append(nullToEmpty(candidate.stockStatus()))
				.append("\n"));

		return builder.toString();
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

	private String truncate(String value, int maxLength) {
		if (value == null || value.isBlank()) {
			return "";
		}

		String normalizedValue = value.trim();

		if (normalizedValue.length() <= maxLength) {
			return normalizedValue;
		}

		return normalizedValue.substring(0, maxLength) + "...";
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}