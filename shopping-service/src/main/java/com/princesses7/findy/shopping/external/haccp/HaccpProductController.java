package com.princesses7.findy.shopping.external.haccp;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HaccpProductController {

	private final HaccpProductClient haccpProductClient;

	// TODO: ApiResponse 사용?
	@GetMapping("/api/v1/haccp/products")
	public List<HaccpProductItemResponse> search(
		@RequestParam String productName
	) {
		return haccpProductClient.searchByProductName(productName);
	}
}