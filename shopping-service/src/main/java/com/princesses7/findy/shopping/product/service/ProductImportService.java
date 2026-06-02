package com.princesses7.findy.shopping.product.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.external.mfds.MfdsBarcodeClient;
import com.princesses7.findy.shopping.external.mfds.MfdsLinkedProductClient;
import com.princesses7.findy.shopping.external.mfds.MfdsProductMapper;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeItemResponse;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeResponse;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsLinkedProductItemResponse;
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
	private final MfdsLinkedProductClient mfdsLinkedProductClient;
	private final MfdsProductMapper mfdsProductMapper;
	private final ProductRepository productRepository;

	@Transactional
	public Long importByBarcode(String barcode) {
		if (productRepository.existsByBarcodeAndDeletedAtIsNull(barcode)) {
			throw new ProductException(ErrorCode.DUPLICATE_BARCODE);
		}

		MfdsBarcodeResponse barcodeResponse = mfdsBarcodeClient.searchByBarcode(barcode);
		List<MfdsBarcodeItemResponse> barcodeItems = barcodeResponse == null
			? List.of()
			: barcodeResponse.getItems();

		Optional<MfdsBarcodeItemResponse> barcodeItem = barcodeItems.stream()
			.findFirst();

		Optional<MfdsLinkedProductItemResponse> linkedItem =
			mfdsLinkedProductClient.searchFirstByBarcode(barcode);

		if (barcodeItem.isEmpty() && linkedItem.isEmpty()) {
			throw new ProductException(ErrorCode.BARCODE_PRODUCT_NOT_FOUND);
		}

		ProductImportCommand command = mfdsProductMapper.toCommand(
			barcode,
			barcodeItem,
			linkedItem
		);

		Product product = Product.create(command);

		return productRepository.save(product).getProductId();
	}
}