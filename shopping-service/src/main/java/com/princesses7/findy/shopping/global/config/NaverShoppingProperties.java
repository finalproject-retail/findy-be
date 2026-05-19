package com.princesses7.findy.shopping.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.shopping")
public record NaverShoppingProperties(
	String baseUrl,
	String clientId,
	String clientSecret
) {
}