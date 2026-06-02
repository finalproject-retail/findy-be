package com.retail.apigateway.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.retail.apigateway.config.GatewayServiceProperties;

class ServiceRouteResolverTest {

	private ServiceRouteResolver resolver;

	@BeforeEach
	void setUp() {
		GatewayServiceProperties properties = new GatewayServiceProperties(
			new GatewayServiceProperties.ServiceEndpoint("http://user"),
			new GatewayServiceProperties.ServiceEndpoint("http://shopping"),
			new GatewayServiceProperties.ServiceEndpoint("http://map"),
			new GatewayServiceProperties.ServiceEndpoint("http://recommendation"),
			new GatewayServiceProperties.ServiceEndpoint("http://analytics")
		);
		resolver = new ServiceRouteResolver(properties);
	}

	@Test
	void routesToRecommendationService() {
		assertThat(resolver.resolveBaseUrl("/api/v1/recommendations"))
			.isEqualTo("http://recommendation");
		assertThat(resolver.resolveBaseUrl("/api/v1/recommendations/logs"))
			.isEqualTo("http://recommendation");
	}

	@Test
	void routesToAnalyticsService() {
		assertThat(resolver.resolveBaseUrl("/api/v1/analytics/products"))
			.isEqualTo("http://analytics");
	}

	@Test
	void routesUnmatchedPathsToShoppingService() {
		assertThat(resolver.resolveBaseUrl("/api/v1/products"))
			.isEqualTo("http://shopping");
		assertThat(resolver.resolveBaseUrl("/api/v1/carts"))
			.isEqualTo("http://shopping");
	}
}
