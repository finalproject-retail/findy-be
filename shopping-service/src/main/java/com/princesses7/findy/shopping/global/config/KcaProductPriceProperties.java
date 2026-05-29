package com.princesses7.findy.shopping.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kca.product-price")
public record KcaProductPriceProperties(
	String baseUrl,
	String serviceKey
) {
}