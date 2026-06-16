package com.princesses7.findy.recommendation.external.bedrock;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.external.rerank.RelatedProductRerankClient;
import com.princesses7.findy.recommendation.global.config.BedrockProperties;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.recommendation.dto.response.RelatedProductRerankItem;
import com.princesses7.findy.recommendation.recommendation.dto.response.RelatedProductRerankResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InferenceConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "bedrock")
public class BedrockRelatedProductRerankClient implements RelatedProductRerankClient {

	private final BedrockRuntimeClient bedrockRuntimeClient;
	private final BedrockProperties properties;
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
			ConverseRequest request = ConverseRequest.builder()
				.modelId(properties.chatModelId())
				.system(SystemContentBlock.builder().text(systemPrompt()).build())
				.messages(Message.builder()
					.role(ConversationRole.USER)
					.content(ContentBlock.builder().text(userPrompt(
						sourceProduct,
						sourceCategoryName,
						candidates,
						categoryNameMap,
						size
					)).build())
					.build())
				.inferenceConfig(InferenceConfiguration.builder()
					.maxTokens(properties.maxTokens())
					.temperature(properties.temperature())
					.build())
				.build();

			ConverseResponse response = bedrockRuntimeClient.converse(request);
			String content = extractText(response);

			if (content.isBlank()) {
				return List.of();
			}

			RelatedProductRerankResponse rerankResponse = objectMapper.readValue(
				cleanJson(content),
				RelatedProductRerankResponse.class
			);

			return rerankResponse.safeRecommendations();
		} catch (Exception exception) {
			log.warn("Bedrock related product rerank failed. modelId={}, message={}",
				properties.chatModelId(),
				exception.getMessage(),
				exception
			);
			return List.of();
		}
	}

	private String extractText(ConverseResponse response) {
		if (response == null || response.output() == null || response.output().message() == null) {
			return "";
		}

		return response.output()
			.message()
			.content()
			.stream()
			.map(ContentBlock::text)
			.filter(Objects::nonNull)
			.collect(java.util.stream.Collectors.joining("\n"))
			.trim();
	}

	private String systemPrompt() {
		return """
			당신은 오프라인 대형마트 상품 상세페이지의 연관 상품 추천 재정렬기입니다.
			
			추천 위치:
			- 상품 상세페이지의 "함께 보면 좋은 상품" 영역입니다.
			- 사용자가 요리 재료나 레시피를 요청한 상황이 아닙니다.
			
			목표:
			- 기준 상품을 보고 있는 사용자가 실제 장보기 상황에서 함께 구매하거나 함께 소비하기 자연스러운 상품만 선택하세요.
			- 새로운 요리명, 레시피, 조리 상황을 임의로 만들지 마세요.
			
			관계 라벨:
			- COMPLEMENTARY: 기준 상품과 함께 구매하거나 함께 소비하기 자연스러운 보완 상품
			- SUBSTITUTE: 기준 상품을 대체할 수 있는 유사 상품
			- RECIPE_INGREDIENT: 기준 상품과 직접 함께 소비하기보다는 특정 요리나 레시피를 만들기 위한 재료에 가까운 상품
			- UNRELATED: 관계가 약하거나, 넓은 의미에서만 비슷하거나, 상품 상세 연관 추천으로 부적절한 상품
			
			규칙:
			- 상품 상세페이지에는 COMPLEMENTARY 상품만 추천하세요.
			- SUBSTITUTE, RECIPE_INGREDIENT, UNRELATED는 추천하지 마세요.
			- 특정 요리의 재료로만 연결되는 상품은 RECIPE_INGREDIENT로 판단하세요.
			- 기준 상품명이나 후보 상품명에 없는 요리명을 reason에 만들지 마세요.
			- "함께 조리하면 좋다"는 이유만으로 추천하지 마세요.
			- 실제로 바로 함께 먹거나 함께 담기 자연스러운 경우만 COMPLEMENTARY로 판단하세요.
			- productId를 절대 지어내지 마세요.
			- 반드시 후보 상품의 productId만 사용하세요.
			- 응답은 JSON 객체 하나만 반환하세요.
			- 마크다운 코드블록이나 설명 문장을 포함하지 마세요.
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
			후보 상품을 상품 상세페이지 연관 추천 기준으로 평가하세요.
			COMPLEMENTARY로 판단되는 상품만 최대 %d개 선택하세요.
			SUBSTITUTE, RECIPE_INGREDIENT, UNRELATED는 recommendations에 포함하지 마세요.
			relationScore는 0.0 이상 1.0 이하 값이어야 합니다.
			reason은 상품 상세페이지에 노출될 짧은 추천 사유로 작성하세요.
			새로운 요리명이나 레시피 맥락을 만들지 마세요.
			
			응답 JSON 형식:
			{
			  "recommendations": [
			    {
			      "productId": 123,
			      "relationScore": 0.92,
			      "relationType": "COMPLEMENTARY",
			      "reason": "함께 구매하면 좋은 짧은 이유"
			    }
			  ]
			}
			""".formatted(
			sourceProduct.getProductId(),
			nullToEmpty(sourceProduct.getProductName()),
			nullToEmpty(sourceProduct.getBrandName()),
			nullToEmpty(sourceCategoryName),
			nullToEmpty(sourceProduct.getDescription()),
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