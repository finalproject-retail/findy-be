package com.princesses7.findy.recommendation.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.recommendation.external.shopping.ShoppingServiceProperties;

@Configuration
public class RestClientConfig {

	@Bean
	@Primary
	public RestClient openAiRestClient(OpenAiProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.build();
	}

	@Bean
	public RestClient shoppingRestClient(ShoppingServiceProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.build();
	}
}
