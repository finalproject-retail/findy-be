package com.princesses7.findy.shopping.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "bedrock")
public class BedrockConfig {

	@Bean
	public BedrockRuntimeClient bedrockRuntimeClient(BedrockProperties properties) {
		return BedrockRuntimeClient.builder()
			.region(Region.of(properties.region()))
			.build();
	}
}
