package com.princesses7.findy.shopping.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "recommendation.service")
public record RecommendationServiceProperties(
	String baseUrl
) {

	public String resolvedBaseUrl() {
		if (!StringUtils.hasText(baseUrl)) {
			return "http://localhost:8886";
		}

		return baseUrl;
	}
}