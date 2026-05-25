package com.princesses7.findy.recommendation.external.openai;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatRequest;
import com.princesses7.findy.recommendation.external.openai.dto.response.OpenAiChatResponse;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.recommendation.dto.response.RelatedProductRerankItem;
import com.princesses7.findy.recommendation.recommendation.dto.response.RelatedProductRerankResponse;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OpenAiRelatedProductRerankClient {

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;
	private final ObjectMapper objectMapper;

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
			너는 오프라인 대형마트의 장바구니 연관 상품 추천 검수 모델이다.
			
			목표:
			기준 상품과 함께 구매하기 좋은 보완재만 고른다.
			
			관계 분류:
			- COMPLEMENTARY: 기준 상품과 함께 먹거나, 함께 조리하거나, 같은 식사 목적에서 같이 담기 좋은 상품
			- SUBSTITUTE: 기준 상품을 대체하는 비슷한 상품
			- UNRELATED: 식품이라는 점만 비슷하거나, 건강식/간편식처럼 넓은 의미만 비슷한 상품
			
			중요 규칙:
			- 대체재는 추천하지 않는다.
			- 단순히 "식품", "간편식", "건강식" 맥락만 비슷한 상품은 제외한다.
			- 실제 장바구니에서 함께 담길 이유가 약하면 UNRELATED로 분류한다.
			- 후보 목록에 있는 productId만 사용한다.
			- 반드시 JSON 객체만 응답한다.
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
			후보 상품 중 기준 상품과 함께 구매하기 좋은 COMPLEMENTARY 상품만 최대 %d개 선택해줘.
			relationScore는 0.0부터 1.0 사이로 줘.
			관계가 애매하면 추천하지 말고 제외해.
			
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