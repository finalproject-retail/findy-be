package com.princesses7.findy.recommendation.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bedrock")
public record BedrockProperties(
	String region,
	String chatModelId,
	String embeddingModelId,
	Integer embeddingDimensions,
	Integer maxTokens,
	Float temperature
) {

	public String region() {
		return region == null ? "ap-northeast-2" : region;
	}

	public String chatModelId() {
		return chatModelId == null ? "anthropic.claude-3-haiku-20240307-v1:0" : chatModelId;
	}

	public String embeddingModelId() {
		return embeddingModelId == null ? "amazon.titan-embed-text-v2:0" : embeddingModelId;
	}

	public Integer embeddingDimensions() {
		return embeddingDimensions == null ? 512 : embeddingDimensions;
	}

	public Integer maxTokens() {
		return maxTokens == null ? 1200 : maxTokens;
	}

	public Float temperature() {
		return temperature == null ? 0.0F : temperature;
	}
}
