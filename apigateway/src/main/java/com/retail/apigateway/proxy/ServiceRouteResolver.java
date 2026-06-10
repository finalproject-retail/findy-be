package com.retail.apigateway.proxy;

import org.springframework.stereotype.Component;

import com.retail.apigateway.config.GatewayServiceProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceRouteResolver {

	private final GatewayServiceProperties gatewayServiceProperties;

	public String resolveBaseUrl(String requestUri) {
		if (requestUri.startsWith("/api/v1/auth") || requestUri.startsWith("/api/v1/users")) {
			return normalizeBaseUrl(gatewayServiceProperties.user().baseUrl());
		}

		if (requestUri.startsWith("/api/v1/stores")
			|| requestUri.startsWith("/api/v1/beacon-signals")
			|| requestUri.startsWith("/api/v1/path")) {
			return normalizeBaseUrl(gatewayServiceProperties.map().baseUrl());
		}

		if (requestUri.startsWith("/api/v1/recommendations")
			|| requestUri.startsWith("/api/v1/notifications")
			|| requestUri.startsWith("/api/v1/chatbot")) {
			return normalizeBaseUrl(gatewayServiceProperties.recommendation().baseUrl());
		}

		if (requestUri.startsWith("/api/v1/analytics")) {
			return normalizeBaseUrl(gatewayServiceProperties.analytics().baseUrl());
		}

		return normalizeBaseUrl(gatewayServiceProperties.shopping().baseUrl());
	}

	private String normalizeBaseUrl(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			throw new IllegalStateException("서비스 base-url 설정이 필요합니다.");
		}

		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}
}