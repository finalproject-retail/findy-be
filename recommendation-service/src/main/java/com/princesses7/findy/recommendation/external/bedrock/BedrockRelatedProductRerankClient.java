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
		당신은 오프라인 리테일 마트의 상품 추천 재정렬기입니다.
		기준 상품과 함께 구매하면 명확히 유용한 상품만 선택하세요.

		관계 라벨:
		- COMPLEMENTARY: 기준 상품과 함께 요리, 식사, 보관, 청소, 장보기 맥락에서 유용한 상품.
		- SUBSTITUTE: 기준 상품을 대체할 수 있는 유사 상품.
		- UNRELATED: 관계가 약하거나, 일반적이거나, 관련 없는 상품.

		규칙:
		- COMPLEMENTARY 상품만 추천하세요.
		- SUBSTITUTE 상품은 추천하지 마세요.
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
		후보 상품 중 기준 상품과 함께 구매하면 좋은 보완 상품을 최대 %d개 선택하세요.
		relationScore는 0.0 이상 1.0 이하 값이어야 합니다.
		relationType은 반드시 "COMPLEMENTARY"만 사용하세요.

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
