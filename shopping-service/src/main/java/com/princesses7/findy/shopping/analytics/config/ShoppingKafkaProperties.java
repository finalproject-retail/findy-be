package com.princesses7.findy.shopping.analytics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shopping.kafka")
public record ShoppingKafkaProperties(
	boolean enabled,
	String topic,
	Long defaultStoreId
) {

	public ShoppingKafkaProperties {
		if (topic == null || topic.isBlank()) {
			topic = "shopping.analytics.events";
		}
		if (defaultStoreId == null) {
			defaultStoreId = 1L;
		}
	}
}
