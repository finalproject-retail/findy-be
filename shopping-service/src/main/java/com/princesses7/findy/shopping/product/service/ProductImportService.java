package com.princesses7.findy.shopping.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.external.mfds.MfdsBarcodeClient;
import com.princesses7.findy.shopping.external.mfds.MfdsProductMapper;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeItemResponse;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeResponse;
import com.princesses7.findy.shopping.global.exception.ErrorCode;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImportService {

	private final MfdsBarcodeClient mfdsBarcodeClient;
	private final MfdsProductMapper mfdsProductMapper;
	private final ProductRepository productRepository;

	@Transactional
	public Long importByBarcode(String barcode) {
		if (productRepository.existsByBarcodeAndIsDeletedFalse(barcode)) {
			throw new ProductException(ErrorCode.DUPLICATE_BARCODE);
		}

		MfdsBarcodeResponse response = mfdsBarcodeClient.searchByBarcode(barcode);
		List<MfdsBarcodeItemResponse> items = response == null
			? List.of()
			: response.getItems();

		if (items.isEmpty()) {
			throw new ProductException(ErrorCode.BARCODE_PRODUCT_NOT_FOUND);
		}

		ProductImportCommand command = mfdsProductMapper.toCommand(items.get(0));
		Product product = Product.create(command);

		return productRepository.save(product).getProductId();
	}
}