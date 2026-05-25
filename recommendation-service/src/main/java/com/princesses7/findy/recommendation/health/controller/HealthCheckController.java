package com.princesses7.findy.recommendation.health.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.global.response.ApiResponse;

@RestController
public class HealthCheckController {

	@GetMapping("/api/v1/recommendations/health")
	public ApiResponse<Void> health() {
		return ApiResponse.ok("recommendation-service is running");
	}
}