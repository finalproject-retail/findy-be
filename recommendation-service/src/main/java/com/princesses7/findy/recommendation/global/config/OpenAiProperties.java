package com.princesses7.findy.recommendation.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
	String baseUrl,
	String apiKey,
	String embeddingModel,
	Integer embeddingDimensions,
	String chatModel
) {

	public String baseUrl() {
		return baseUrl == null ? "https://api.openai.com" : baseUrl;
	}

	public String embeddingModel() {
		return embeddingModel == null ? "text-embedding-3-small" : embeddingModel;
	}

	public Integer embeddingDimensions() {
		return embeddingDimensions == null ? 512 : embeddingDimensions;
	}

	public String chatModel() {
		return chatModel == null ? "gpt-4o-mini" : chatModel;
	}
}