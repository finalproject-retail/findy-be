package com.princesses7.findy.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class RecommendationServiceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
			.ignoreIfMissing()
			.load();

		dotenv.entries().forEach(entry ->
			System.setProperty(entry.getKey(), entry.getValue())
		);

		SpringApplication.run(RecommendationServiceApplication.class, args);
	}
}