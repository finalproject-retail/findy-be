package com.princesses7.findy.recommendation.external.bedrock;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.external.embedding.ProductEmbeddingClient;
import com.princesses7.findy.recommendation.global.config.BedrockProperties;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "bedrock")
public class BedrockEmbeddingClient implements ProductEmbeddingClient {

	private final BedrockRuntimeClient bedrockRuntimeClient;
	private final BedrockProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public List<Double> createEmbedding(String text) {
		try {
			String requestBody = objectMapper.writeValueAsString(Map.of(
				"inputText", text,
				"dimensions", properties.embeddingDimensions(),
				"normalize", true
			));

			InvokeModelResponse response = bedrockRuntimeClient.invokeModel(InvokeModelRequest.builder()
				.modelId(properties.embeddingModelId())
				.contentType("application/json")
				.accept("application/json")
				.body(SdkBytes.fromString(requestBody, StandardCharsets.UTF_8))
				.build());

			JsonNode body = objectMapper.readTree(response.body().asUtf8String());
			JsonNode embeddingNode = body.path("embedding");

			if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
				throw new BaseException(ErrorCode.RECOMMENDATION_EMPTY_EMBEDDING);
			}

			List<Double> embedding = new ArrayList<>();
			embeddingNode.forEach(value -> embedding.add(value.asDouble()));

			return embedding;
		} catch (BaseException exception) {
			throw exception;
		} catch (Exception exception) {
			log.error("Bedrock embedding request failed. modelId={}, message={}",
				properties.embeddingModelId(),
				exception.getMessage(),
				exception
			);
			throw new BaseException(ErrorCode.RECOMMENDATION_EMBEDDING_FAILED);
		}
	}

	@Override
	public String model() {
		return properties.embeddingModelId();
	}

	@Override
	public int dimensions() {
		return properties.embeddingDimensions();
	}
}
