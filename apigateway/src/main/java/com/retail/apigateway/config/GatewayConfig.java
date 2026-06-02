package com.retail.apigateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.retail.apigateway.auth.GatewayAuthenticationFilter;
import com.retail.apigateway.auth.GatewayJwtProperties;
import com.retail.apigateway.auth.GatewayJwtValidator;
import com.retail.apigateway.auth.GatewayPublicPathMatcher;

@Configuration
@EnableConfigurationProperties({ GatewayServiceProperties.class, GatewayJwtProperties.class })
public class GatewayConfig {

	@Bean
	public GatewayAuthenticationFilter gatewayAuthenticationFilter(
		GatewayPublicPathMatcher publicPathMatcher,
		GatewayJwtValidator jwtValidator
	) {
		return new GatewayAuthenticationFilter(publicPathMatcher, jwtValidator);
	}

	@Bean
	public RestClient restClient() {
		return RestClient.builder().build();
	}
}
