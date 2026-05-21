package com.princesses7.findy.recommendation.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient openAiRestClient(OpenAiProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.build();
	}
}