package com.princesses7.findy.recommendation.external.openai;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiEmbeddingRequest;
import com.princesses7.findy.recommendation.external.openai.dto.response.OpenAiEmbeddingResponse;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiEmbeddingClient {

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;

	public List<Double> createEmbedding(String text) {
		try {
			OpenAiEmbeddingRequest request = OpenAiEmbeddingRequest.of(
				properties.embeddingModel(),
				text,
				properties.embeddingDimensions()
			);

			OpenAiEmbeddingResponse response = openAiRestClient.post()
				.uri("/v1/embeddings")
				.header("Authorization", "Bearer " + properties.apiKey())
				.header("Content-Type", "application/json")
				.body(request)
				.retrieve()
				.body(OpenAiEmbeddingResponse.class);

			if (response == null || response.firstEmbedding().isEmpty()) {
				throw new BaseException(ErrorCode.RECOMMENDATION_EMPTY_EMBEDDING);
			}

			return response.firstEmbedding();
		} catch (BaseException exception) {
			throw exception;
		} catch (Exception exception) {
			log.error("OpenAI embedding request failed. message={}", exception.getMessage(), exception);
			throw new BaseException(ErrorCode.RECOMMENDATION_EMBEDDING_FAILED);
		}
	}
}