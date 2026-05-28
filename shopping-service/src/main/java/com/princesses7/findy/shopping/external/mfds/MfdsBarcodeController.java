package com.princesses7.findy.shopping.external.mfds;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeResponse;
import com.princesses7.findy.shopping.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mfds/barcodes")
public class MfdsBarcodeController {

	private final MfdsBarcodeClient mfdsBarcodeClient;
	private final MfdsLinkedProductClient mfdsLinkedProductClient;

	@GetMapping
	public ApiResponse<MfdsBarcodeResponse> searchByBarcode(@RequestParam String barcode) {
		MfdsBarcodeResponse response = mfdsBarcodeClient.searchByBarcode(barcode);
		return ApiResponse.ok("식약처 바코드 상품 조회가 성공적으로 처리되었습니다.", response);
	}

	@GetMapping("/raw")
	public ApiResponse<String> searchRawByBarcode(@RequestParam String barcode) {
		String response = mfdsBarcodeClient.searchRawByBarcode(barcode);
		return ApiResponse.ok("식약처 바코드 상품 원문 조회가 성공적으로 처리되었습니다.", response);
	}

	@GetMapping("/linked")
	public ApiResponse<Object> searchLinkedProductByBarcode(@RequestParam String barcode) {
		Object response = mfdsLinkedProductClient.searchFirstByBarcode(barcode);
		return ApiResponse.ok("식약처 바코드 연계 상품 조회가 성공적으로 처리되었습니다.", response);
	}

	@GetMapping("/linked/raw")
	public ApiResponse<String> searchLinkedProductRawByBarcode(@RequestParam String barcode) {
		String response = mfdsLinkedProductClient.searchRawByBarcode(barcode);
		return ApiResponse.ok("식약처 바코드 연계 상품 원문 조회가 성공적으로 처리되었습니다.", response);
	}
}