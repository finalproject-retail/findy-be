package com.princesses7.findy.shopping;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.princesses7.findy.shopping.config.TestRedisConfig;

@SpringBootTest(classes = ShoppingServiceApplication.class)
@Import(TestRedisConfig.class)
class ShoppingServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}