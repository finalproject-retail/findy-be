package com.princesses7.findy.shopping.product.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.product.service.ProductImportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/products/import")
public class ProductImportTestController {

	private final ProductImportService productImportService;

	@PostMapping("/mfds")
	public Long importByBarcode(@RequestParam String barcode) {
		return productImportService.importByBarcode(barcode);
	}
}