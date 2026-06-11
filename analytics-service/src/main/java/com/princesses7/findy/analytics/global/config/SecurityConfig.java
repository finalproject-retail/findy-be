package com.princesses7.findy.analytics.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(csrf -> csrf.disable())
			.formLogin(formLogin -> formLogin.disable())
			.httpBasic(httpBasic -> httpBasic.disable())
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/actuator/**").permitAll()
				.requestMatchers("/api/v1/admin/**").permitAll()
				.anyRequest().permitAll()
			)
			.build();
	}
}