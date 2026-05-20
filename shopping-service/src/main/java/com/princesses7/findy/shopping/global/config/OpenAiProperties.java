package com.princesses7.findy.shopping.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
	String apiKey,
	String model
) {
}