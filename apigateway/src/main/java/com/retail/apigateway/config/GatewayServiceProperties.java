package com.retail.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public record GatewayServiceProperties(
	ServiceEndpoint user,
	ServiceEndpoint shopping,
	ServiceEndpoint map,
	ServiceEndpoint recommendation,
	ServiceEndpoint analytics
) {

	public record ServiceEndpoint(
		String baseUrl
	) {
	}
}
