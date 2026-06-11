package com.princesses7.findy.recommendation.global.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.recommendation.external.shopping.ShoppingServiceProperties;

@Configuration
public class RestClientConfig {

	@Bean
	@Primary
	public RestClient openAiRestClient(OpenAiProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.requestFactory(createRequestFactory(
				Duration.ofSeconds(properties.connectTimeoutSeconds()),
				Duration.ofSeconds(properties.readTimeoutSeconds())
			))
			.build();
	}

	@Bean
	public RestClient shoppingRestClient(ShoppingServiceProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.requestFactory(createRequestFactory(Duration.ofSeconds(2), Duration.ofSeconds(5)))
			.build();
	}

	private SimpleClientHttpRequestFactory createRequestFactory(
		Duration connectTimeout,
		Duration readTimeout
	) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeout);
		requestFactory.setReadTimeout(readTimeout);
		return requestFactory;
	}
}
