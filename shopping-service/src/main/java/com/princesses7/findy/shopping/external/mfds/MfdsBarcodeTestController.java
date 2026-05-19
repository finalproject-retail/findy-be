package com.princesses7.findy.shopping.external.mfds;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/mfds/barcodes")
public class MfdsBarcodeTestController {

	private final MfdsBarcodeClient mfdsBarcodeClient;

	@GetMapping
	public MfdsBarcodeResponse searchByBarcode(@RequestParam String barcode) {
		return mfdsBarcodeClient.searchByBarcode(barcode);
	}
}