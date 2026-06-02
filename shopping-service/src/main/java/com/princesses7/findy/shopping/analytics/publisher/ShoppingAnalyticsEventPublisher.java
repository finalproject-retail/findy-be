package com.princesses7.findy.shopping.analytics.publisher;

import org.springframework.beans.factory.ObjectProvider;
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

	private final ShoppingKafkaProperties shoppingKafkaProperties;
	private final ObjectProvider<KafkaTemplate<String, ShoppingAnalyticsEvent>> shoppingAnalyticsKafkaTemplateProvider;

	public void publish(ShoppingAnalyticsEvent event) {
		if (!shoppingKafkaProperties.enabled()) {
			return;
		}

		if (event.userId() == null) {
			log.debug("Skip analytics event without userId. eventType={}", event.eventType());
			return;
		}

		Runnable sendTask = () -> send(event);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					sendTask.run();
				}
			});
			return;
		}

		sendTask.run();
	}

	private void send(ShoppingAnalyticsEvent event) {
		KafkaTemplate<String, ShoppingAnalyticsEvent> kafkaTemplate =
			shoppingAnalyticsKafkaTemplateProvider.getIfAvailable();
		if (kafkaTemplate == null) {
			log.debug("KafkaTemplate not available. eventType={}", event.eventType());
			return;
		}

		try {
			kafkaTemplate.send(
				shoppingKafkaProperties.topic(),
				String.valueOf(event.userId()),
				event
			).whenComplete((result, exception) -> {
				if (exception != null) {
					log.warn(
						"Failed to publish shopping analytics event. eventType={}, userId={}",
						event.eventType(),
						event.userId(),
						exception
					);
				}
			});
		} catch (RuntimeException exception) {
			log.warn(
				"Failed to publish shopping analytics event. eventType={}, userId={}",
				event.eventType(),
				event.userId(),
				exception
			);
		}
	}
}
