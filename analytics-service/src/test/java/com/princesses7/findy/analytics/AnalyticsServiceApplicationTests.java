package com.princesses7.findy.analytics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "analytics.kafka.enabled=false")
class AnalyticsServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
