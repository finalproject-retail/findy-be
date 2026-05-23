package com.retail.map_service.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retail.map_service.dto.request.BeaconSignalCreateRequest;
import com.retail.map_service.dto.response.BeaconSignalLogResponse;
import com.retail.map_service.global.response.ApiResponse;
import com.retail.map_service.service.BeaconSignalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/beacon-signals")
@RequiredArgsConstructor
public class BeaconSignalController {

	private final BeaconSignalService beaconSignalService;

	@PostMapping
	public ApiResponse<BeaconSignalLogResponse> recordGridChange(
			@Valid @RequestBody BeaconSignalCreateRequest request,
			Authentication authentication
	) {
		Long userId = Long.parseLong(authentication.getName());
		return ApiResponse.ok(beaconSignalService.recordGridChange(userId, request));
	}
}
