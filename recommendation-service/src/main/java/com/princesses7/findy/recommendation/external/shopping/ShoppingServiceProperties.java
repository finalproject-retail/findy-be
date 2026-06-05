package com.princesses7.findy.recommendation.external.shopping;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.shopping")
public record ShoppingServiceProperties(
	String baseUrl
) {

	public String baseUrl() {
		return baseUrl == null ? "http://localhost:8887" : baseUrl;
	}
}
