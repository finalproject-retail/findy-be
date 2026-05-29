package com.retail.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.shopping")
public record ShoppingServiceProperties(
	String baseUrl
) {
}
