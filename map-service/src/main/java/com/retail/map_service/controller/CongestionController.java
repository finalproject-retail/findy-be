package com.retail.map_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retail.map_service.dto.response.GridCongestionListResponse;
import com.retail.map_service.dto.response.StoreCongestionResponse;
import com.retail.map_service.global.response.ApiResponse;
import com.retail.map_service.service.CongestionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class CongestionController {

	private final CongestionService congestionService;

	@GetMapping("/{storeId}/congestion")
	public ApiResponse<StoreCongestionResponse> getStoreCongestion(
		@PathVariable Long storeId,
		@RequestParam(required = false) Integer windowSeconds,
		@RequestParam(required = false) Integer threshold
	) {
		return ApiResponse.ok(congestionService.getStoreCongestion(storeId, windowSeconds, threshold));
	}

	@GetMapping("/{storeId}/grids/congestion")
	public ApiResponse<GridCongestionListResponse> getGridCongestion(
		@PathVariable Long storeId,
		@RequestParam(required = false) Integer windowSeconds,
		@RequestParam(required = false) Integer threshold
	) {
		return ApiResponse.ok(congestionService.getGridCongestion(storeId, windowSeconds, threshold));
	}
}
