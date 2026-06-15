package com.princesses7.findy.analytics.event.listener;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.princesses7.findy.analytics.event.dto.ShoppingAnalyticsEventPayload;
import com.princesses7.findy.analytics.event.service.ShoppingAnalyticsEventConsumerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "analytics.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ShoppingAnalyticsKafkaListener {

	private final ObjectMapper objectMapper;
	private final ShoppingAnalyticsEventConsumerService consumerService;

	@KafkaListener(
		topics = "${analytics.kafka.topic:shopping.analytics.events}",
		groupId = "${spring.kafka.consumer.group-id:${spring.application.name}}"
	)
	public void listen(String message) {
		try {
			ShoppingAnalyticsEventPayload event = objectMapper.readValue(
				message,
				ShoppingAnalyticsEventPayload.class
			);

			consumerService.consume(event);
		} catch (JsonProcessingException exception) {
			log.warn("Failed to parse shopping analytics Kafka event. payload={}", message, exception);
		}
	}
}
