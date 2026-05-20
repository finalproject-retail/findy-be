package com.princesses7.findy.shopping.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.ObjectMapper;

@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}