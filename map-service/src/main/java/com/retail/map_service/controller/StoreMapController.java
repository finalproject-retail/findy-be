package com.retail.map_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retail.map_service.dto.response.StoreMapConfigResponse;
import com.retail.map_service.global.response.ApiResponse;
import com.retail.map_service.service.StoreMapConfigService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreMapController {

	private final StoreMapConfigService storeMapConfigService;

	@GetMapping("/{storeId}/map-config")
	public ApiResponse<StoreMapConfigResponse> getMapConfig(@PathVariable Long storeId) {
		return ApiResponse.ok(storeMapConfigService.getMapConfig(storeId));
	}
}
