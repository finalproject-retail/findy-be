package com.princesses7.findy.shopping.external.kca;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceResponse;
import com.princesses7.findy.shopping.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kca/product-prices")
public class KcaProductPriceController {

	private final KcaProductPriceClient kcaProductPriceClient;

	@GetMapping
	public ApiResponse<KcaProductPriceResponse> getProductPrices(
		@RequestParam(required = false) String goodInspectDay,
		@RequestParam(required = false) String entpId,
		@RequestParam(required = false) String goodId
	) {
		KcaProductPriceResponse response = findProductPrices(goodInspectDay, entpId, goodId);

		return ApiResponse.ok("한국소비자원 생필품 가격 정보 조회에 성공했습니다.", response);
	}

	@GetMapping("/raw")
	public String getRawProductPrices(
		@RequestParam String goodInspectDay,
		@RequestParam(required = false) String entpId,
		@RequestParam(required = false) String goodId
	) {
		return kcaProductPriceClient.getRawProductPrices(goodInspectDay, entpId, goodId);
	}

	private KcaProductPriceResponse findProductPrices(
		String goodInspectDay,
		String entpId,
		String goodId
	) {
		if (isBlank(goodInspectDay)) {
			return kcaProductPriceClient.getLatestProductPrices(entpId, goodId);
		}

		return kcaProductPriceClient.getProductPrices(goodInspectDay, entpId, goodId);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}