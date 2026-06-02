package com.princesses7.findy.shopping.analytics.publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.princesses7.findy.shopping.analytics.config.ShoppingKafkaProperties;
import com.princesses7.findy.shopping.analytics.event.ShoppingAnalyticsEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShoppingAnalyticsEventPublisher {

	private final KafkaTemplate<String, ShoppingAnalyticsEvent> kafkaTemplate;
	private final ShoppingKafkaProperties shoppingKafkaProperties;

	public void publish(ShoppingAnalyticsEvent event) {
		if (event == null) {
			return;
		}

		if (!shoppingKafkaProperties.enabled()) {
			log.info(
				"Skip shopping analytics event because Kafka is disabled. eventType={}, userId={}",
				event.eventType(),
				event.userId()
			);
			return;
		}

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			log.info(
				"Publish shopping analytics event immediately. eventType={}, userId={}, topic={}",
				event.eventType(),
				event.userId(),
				shoppingKafkaProperties.topic()
			);
			send(event);
			return;
		}

		log.info(
			"Register shopping analytics event after commit. eventType={}, userId={}, topic={}",
			event.eventType(),
			event.userId(),
			shoppingKafkaProperties.topic()
		);

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				log.info(
					"Publish shopping analytics event after commit. eventType={}, userId={}, topic={}",
					event.eventType(),
					event.userId(),
					shoppingKafkaProperties.topic()
				);
				send(event);
			}
		});
	}

	public void publishAfterCommit(ShoppingAnalyticsEvent event) {
		publish(event);
	}

	private void send(ShoppingAnalyticsEvent event) {
		try {
			kafkaTemplate.send(
				shoppingKafkaProperties.topic(),
				String.valueOf(event.userId()),
				event
			).whenComplete((result, exception) -> {
				if (exception != null) {
					log.warn(
						"Failed to publish shopping analytics event. eventType={}, userId={}, topic={}",
						event.eventType(),
						event.userId(),
						shoppingKafkaProperties.topic(),
						exception
					);
					return;
				}

				log.info(
					"Published shopping analytics event. eventType={}, userId={}, topic={}, partition={}, offset={}",
					event.eventType(),
					event.userId(),
					result.getRecordMetadata().topic(),
					result.getRecordMetadata().partition(),
					result.getRecordMetadata().offset()
				);
			});
		} catch (RuntimeException exception) {
			log.warn(
				"Failed to publish shopping analytics event. eventType={}, userId={}, topic={}",
				event.eventType(),
				event.userId(),
				shoppingKafkaProperties.topic(),
				exception
			);
		}
	}
}