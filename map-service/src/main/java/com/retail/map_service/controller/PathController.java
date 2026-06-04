package com.retail.map_service.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retail.map_service.dto.request.PathNavigationRequest;
import com.retail.map_service.dto.response.PathNavigationResponse;
import com.retail.map_service.global.response.ApiResponse;
import com.retail.map_service.service.StorePathService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/path")
@RequiredArgsConstructor
public class PathController {

	private final StorePathService storePathService;

	@PostMapping
	public ApiResponse<PathNavigationResponse> navigate(
		@Valid @RequestBody PathNavigationRequest request,
		Authentication authentication
	) {
		Long userId = Long.parseLong(authentication.getName());
		PathNavigationResponse response = storePathService.navigate(userId, request);
		return ApiResponse.ok("매장 내 경로 안내에 성공했습니다.", response);
	}
}
