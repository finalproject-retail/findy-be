package com.retail.map_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retail.map_service.dto.response.StoreResponse;
import com.retail.map_service.global.response.ApiResponse;
import com.retail.map_service.service.StoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

	private final StoreService storeService;

	@GetMapping
	public ApiResponse<List<StoreResponse>> getStores() {
		return ApiResponse.ok(storeService.getActiveStores());
	}

	@GetMapping("/{storeId}")
	public ApiResponse<StoreResponse> getStore(@PathVariable Long storeId) {
		return ApiResponse.ok(storeService.getStore(storeId));
	}
}
