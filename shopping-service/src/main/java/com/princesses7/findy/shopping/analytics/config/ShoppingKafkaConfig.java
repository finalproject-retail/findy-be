package com.princesses7.findy.shopping.analytics.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.princesses7.findy.shopping.analytics.event.ShoppingAnalyticsEvent;

@Configuration
@EnableConfigurationProperties(ShoppingKafkaProperties.class)
@ConditionalOnProperty(prefix = "shopping.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ShoppingKafkaConfig {

	@Bean
	ProducerFactory<String, ShoppingAnalyticsEvent> shoppingAnalyticsProducerFactory(
		@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
	) {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	KafkaTemplate<String, ShoppingAnalyticsEvent> shoppingAnalyticsKafkaTemplate(
		ProducerFactory<String, ShoppingAnalyticsEvent> shoppingAnalyticsProducerFactory
	) {
		return new KafkaTemplate<>(shoppingAnalyticsProducerFactory);
	}
}
