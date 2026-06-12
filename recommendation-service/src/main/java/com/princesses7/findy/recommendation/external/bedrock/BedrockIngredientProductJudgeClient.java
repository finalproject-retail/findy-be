package com.princesses7.findy.recommendation.external.bedrock;

import java.util.List;
import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;
import com.princesses7.findy.recommendation.chatbot.dto.IngredientProductJudgeItem;
import com.princesses7.findy.recommendation.chatbot.dto.IngredientProductJudgeResponse;
import com.princesses7.findy.recommendation.chatbot.external.IngredientProductJudgeClient;
import com.princesses7.findy.recommendation.global.config.BedrockProperties;

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
public class BedrockIngredientProductJudgeClient implements IngredientProductJudgeClient {

	private final BedrockRuntimeClient bedrockRuntimeClient;
	private final BedrockProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public List<IngredientProductJudgeItem> judge(
		String recipeName,
		String ingredientName,
		List<ChatbotShoppingProduct> candidates,
		int limit
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
						recipeName,
						ingredientName,
						candidates,
						limit
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

			IngredientProductJudgeResponse judgeResponse = objectMapper.readValue(
				cleanJson(content),
				IngredientProductJudgeResponse.class
			);

			return judgeResponse.safeItems();
		} catch (Exception exception) {
			log.warn("Bedrock ingredient product judge failed. message={}", exception.getMessage(), exception);
			return List.of();
		}
	}

	private String systemPrompt() {
		return """
			당신은 대형마트 챗봇의 레시피 재료 상품 검수 모델입니다.
			
			목표:
			- 사용자가 특정 요리를 만들기 위해 ingredientName 재료를 찾고 있습니다.
			- 후보 상품 목록 전체를 보고, 해당 ingredientName 재료 자체로 사용할 수 있는 상품만 고르세요.
			
			가장 중요한 판단 기준:
			- suitable=true: 후보 상품이 ingredientName 재료 자체로 바로 조리에 사용할 수 있는 상품입니다.
			- suitable=false: 후보 상품이 완제품, 즉석식품, 레토르트, 밀키트, 소스 조합, 간식, 다른 요리, 해당 재료가 일부 들어간 가공식품입니다.
			
			엄격 제외 규칙:
			- 요리명과 같은 완제품은 반드시 제외하세요.
			- 예: recipeName=김치찌개, ingredientName=김치일 때 레토르트 김치찌개, 즉석 김치찌개, 김치찌개 밀키트는 suitable=false입니다.
			- 예: ingredientName=김치일 때 김치라면, 김치사발면, 김치볶음밥, 김치만두는 suitable=false입니다.
			- 예: ingredientName=감자일 때 감자면, 감자칩, 감자스낵은 suitable=false입니다.
			- 예: ingredientName=카레가루일 때 카레라면, 카레볶음밥, 카레 완제품은 suitable=false입니다.
			- 상품명에 재료명이 들어 있어도 재료 자체가 아니면 반드시 suitable=false입니다.
			
			허용 예시:
			- ingredientName=김치: 포기김치, 배추김치, 맛김치처럼 김치 자체 상품은 suitable=true입니다.
			- ingredientName=두부: 찌개용 두부, 부침두부, 순두부처럼 두부 자체 상품은 suitable=true입니다.
			- ingredientName=돼지고기: 삼겹살, 앞다리살, 목살처럼 돼지고기 원재료 상품은 suitable=true입니다.
			- ingredientName=대파: 대파, 손질대파처럼 대파 자체 상품은 suitable=true입니다.
			
			응답 규칙:
			- 후보 목록에 있는 productId만 응답하세요.
			- 적합한 상품이 없으면 {"items": []}로 응답하세요.
			- items에는 suitable=true라고 판단한 상품만 넣으세요.
			- confidence는 0.0 이상 1.0 이하로 주세요.
			- suitable=true 상품을 우선순위 높은 순서로 앞에 두세요.
			- 반드시 JSON 객체 하나만 반환하세요.
			- 마크다운 코드블록이나 설명 문장을 포함하지 마세요.
			""";
	}

	private String userPrompt(
		String recipeName,
		String ingredientName,
		List<ChatbotShoppingProduct> candidates,
		int limit
	) {
		return """
			요리명: %s
			찾는 재료명: %s
			
			후보 상품:
			%s
			
			요청:
			각 후보 상품이 "%s" 재료 자체로 사용 가능한 상품인지 판단하세요.
			suitable=true 상품만 최대 %d개가 최종 추천에 사용됩니다.
			
			응답 JSON 형식:
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
			""".formatted(
			nullToEmpty(recipeName),
			nullToEmpty(ingredientName),
			createCandidateText(candidates),
			nullToEmpty(ingredientName),
			limit
		);
	}

	private String createCandidateText(List<ChatbotShoppingProduct> candidates) {
		StringBuilder builder = new StringBuilder();

		for (ChatbotShoppingProduct candidate : candidates) {
			builder.append("- productId: ")
				.append(candidate.productId())
				.append(", productName: ")
				.append(nullToEmpty(candidate.productName()))
				.append(", brandName: ")
				.append(nullToEmpty(candidate.brandName()))
				.append(", categoryName: ")
				.append(nullToEmpty(candidate.categoryName()))
				.append(", salesUnit: ")
				.append(nullToEmpty(candidate.salesUnit()))
				.append(", volume: ")
				.append(nullToEmpty(candidate.volume()))
				.append(", badgeText: ")
				.append(nullToEmpty(candidate.badgeText()))
				.append(", saleStatus: ")
				.append(nullToEmpty(candidate.saleStatus()))
				.append(", stockStatus: ")
				.append(nullToEmpty(candidate.stockStatus()))
				.append("\n");
		}

		return builder.toString();
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

	private String cleanJson(String content) {
		if (content == null || content.isBlank()) {
			return "{}";
		}

		return content
			.replace("```json", "")
			.replace("```", "")
			.trim();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}