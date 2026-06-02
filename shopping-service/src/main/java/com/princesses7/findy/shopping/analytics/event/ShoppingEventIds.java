package com.princesses7.findy.shopping.analytics.event;

import java.time.Instant;
import java.util.UUID;

final class ShoppingEventIds {

	private ShoppingEventIds() {
	}

	static String newEventId() {
		return UUID.randomUUID().toString();
	}

	static Instant now() {
		return Instant.now();
	}
}
