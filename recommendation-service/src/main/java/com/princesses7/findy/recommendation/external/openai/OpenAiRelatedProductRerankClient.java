package com.princesses7.findy.recommendation.external.openai;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatRequest;
import com.princesses7.findy.recommendation.external.openai.dto.response.OpenAiChatResponse;
import com.princesses7.findy.recommendation.external.rerank.RelatedProductRerankClient;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.recommendation.dto.response.RelatedProductRerankItem;
import com.princesses7.findy.recommendation.recommendation.dto.response.RelatedProductRerankResponse;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "openai", matchIfMissing = true)
public class OpenAiRelatedProductRerankClient implements RelatedProductRerankClient {

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public List<RelatedProductRerankItem> rerank(
		ProductSnapshot sourceProduct,
		String sourceCategoryName,
		List<ProductSnapshot> candidates,
		Map<Long, String> categoryNameMap,
		int size
	) {
		if (candidates == null || candidates.isEmpty()) {
			return List.of();
		}

		try {
			OpenAiChatRequest request = OpenAiChatRequest.json(
				properties.chatModel(),
				List.of(
					OpenAiChatMessage.system(systemPrompt()),
					OpenAiChatMessage.user(userPrompt(
						sourceProduct,
						sourceCategoryName,
						candidates,
						categoryNameMap,
						size
					))
				)
			);

			OpenAiChatResponse response = openAiRestClient.post()
				.uri("/v1/chat/completions")
				.header("Authorization", "Bearer " + properties.apiKey())
				.header("Content-Type", "application/json")
				.body(request)
				.retrieve()
				.body(OpenAiChatResponse.class);

			if (response == null || response.firstContent().isBlank()) {
				return List.of();
			}

			RelatedProductRerankResponse rerankResponse = objectMapper.readValue(
				cleanJson(response.firstContent()),
				RelatedProductRerankResponse.class
			);

			return rerankResponse.safeRecommendations();
		} catch (Exception exception) {
			return List.of();
		}
	}

	private String systemPrompt() {
		return """
			너는 오프라인 대형마트 상품 상세페이지의 연관 상품 추천 검수 모델이다.
			
			추천 위치:
			- 상품 상세페이지의 "함께 보면 좋은 상품" 영역이다.
			- 사용자가 요리 재료나 레시피를 요청한 상황이 아니다.
			
			목표:
			- 기준 상품을 보고 있는 사용자가 실제 장보기 상황에서 함께 구매하거나 함께 소비하기 자연스러운 상품만 고른다.
			- 새로운 요리명, 레시피, 조리 상황을 임의로 만들지 않는다.
			
			관계 분류:
			- COMPLEMENTARY: 기준 상품과 함께 구매하거나 함께 소비하기 자연스러운 보완 상품
			- SUBSTITUTE: 기준 상품을 대체할 수 있는 유사 상품
			- RECIPE_INGREDIENT: 기준 상품과 직접 함께 소비하기보다는 특정 요리나 레시피를 만들기 위한 재료에 가까운 상품
			- UNRELATED: 관계가 약하거나, 넓은 의미에서만 비슷하거나, 상품 상세 연관 추천으로 부적절한 상품
			
			중요 규칙:
			- 상품 상세페이지에는 COMPLEMENTARY만 추천한다.
			- SUBSTITUTE, RECIPE_INGREDIENT, UNRELATED는 추천하지 않는다.
			- 특정 요리의 재료로만 연결되는 상품은 RECIPE_INGREDIENT로 판단한다.
			- 기준 상품명이나 후보 상품명에 없는 요리명을 reason에 만들지 않는다.
			- "함께 조리하면 좋다"는 이유만으로 추천하지 않는다.
			- 실제로 바로 함께 먹거나 함께 담기 자연스러운 경우만 COMPLEMENTARY로 판단한다.
			- 후보 목록에 있는 productId만 사용한다.
			- 반드시 JSON 객체만 응답한다.
			- 마크다운 코드블록이나 설명 문장을 포함하지 않는다.
			""";
	}

	private String userPrompt(
		ProductSnapshot sourceProduct,
		String sourceCategoryName,
		List<ProductSnapshot> candidates,
		Map<Long, String> categoryNameMap,
		int size
	) {
		return """
			기준 상품:
			- productId: %d
			- productName: %s
			- brandName: %s
			- categoryName: %s
			- description: %s
			- packagingType: %s
			- salesUnit: %s
			- volume: %s
			- badgeText: %s
			
			후보 상품:
			%s
			
			요청:
			후보 상품을 상품 상세페이지 연관 추천 기준으로 평가해줘.
			COMPLEMENTARY로 판단되는 상품만 최대 %d개 선택해줘.
			SUBSTITUTE, RECIPE_INGREDIENT, UNRELATED는 recommendations에 포함하지 마.
			relationScore는 0.0부터 1.0 사이로 줘.
			reason은 상품 상세페이지에 노출될 짧은 추천 사유로 작성해줘.
			새로운 요리명이나 레시피 맥락을 만들지 마.
			
			응답 JSON 형식:
			{
			  "recommendations": [
			    {
			      "productId": 123,
			      "relationScore": 0.92,
			      "relationType": "COMPLEMENTARY",
			      "reason": "기준 상품과 함께 구매하기 좋은 이유"
			    }
			  ]
			}
			""".formatted(
			sourceProduct.getProductId(),
			nullToEmpty(sourceProduct.getProductName()),
			nullToEmpty(sourceProduct.getBrandName()),
			nullToEmpty(sourceCategoryName),
			nullToEmpty(sourceProduct.getDescription()),
			nullToEmpty(sourceProduct.getPackagingType()),
			nullToEmpty(sourceProduct.getSalesUnit()),
			nullToEmpty(sourceProduct.getVolume()),
			nullToEmpty(sourceProduct.getBadgeText()),
			createCandidateText(candidates, categoryNameMap),
			size
		);
	}

	private String createCandidateText(
		List<ProductSnapshot> candidates,
		Map<Long, String> categoryNameMap
	) {
		StringBuilder builder = new StringBuilder();

		for (ProductSnapshot candidate : candidates) {
			builder.append("- productId: ")
				.append(candidate.getProductId())
				.append(", productName: ")
				.append(nullToEmpty(candidate.getProductName()))
				.append(", brandName: ")
				.append(nullToEmpty(candidate.getBrandName()))
				.append(", categoryName: ")
				.append(nullToEmpty(categoryNameMap.get(candidate.getCategoryId())))
				.append(", description: ")
				.append(nullToEmpty(candidate.getDescription()))
				.append(", packagingType: ")
				.append(nullToEmpty(candidate.getPackagingType()))
				.append(", salesUnit: ")
				.append(nullToEmpty(candidate.getSalesUnit()))
				.append(", volume: ")
				.append(nullToEmpty(candidate.getVolume()))
				.append(", badgeText: ")
				.append(nullToEmpty(candidate.getBadgeText()))
				.append("\n");
		}

		return builder.toString();
	}

	private String cleanJson(String content) {
		return content
			.replace("```json", "")
			.replace("```", "")
			.trim();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}