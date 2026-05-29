package com.princesses7.findy.shopping.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient naverShoppingRestClient(NaverShoppingProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.build();
	}

	@Bean
	public RestClient mfdsBarcodeRestClient(MfdsBarcodeProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.build();
	}

	@Bean
	public RestClient mfdsLinkedProductRestClient(MfdsLinkedProductProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.build();
	}

	@Bean
	public RestClient kcaProductPriceRestClient(KcaProductPriceProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.build();
	}
}