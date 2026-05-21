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
	private final MfdsLinkedProductClient mfdsLinkedProductClient;

	@GetMapping
	public MfdsBarcodeResponse searchByBarcode(@RequestParam String barcode) {
		return mfdsBarcodeClient.searchByBarcode(barcode);
	}

	@GetMapping("/raw")
	public String searchRawByBarcode(@RequestParam String barcode) {
		return mfdsBarcodeClient.searchRawByBarcode(barcode);
	}

	@GetMapping("/linked")
	public Object searchLinkedProductByBarcode(@RequestParam String barcode) {
		return mfdsLinkedProductClient.searchFirstByBarcode(barcode);
	}

	@GetMapping("/linked/raw")
	public String searchLinkedProductRawByBarcode(@RequestParam String barcode) {
		return mfdsLinkedProductClient.searchRawByBarcode(barcode);
	}
}