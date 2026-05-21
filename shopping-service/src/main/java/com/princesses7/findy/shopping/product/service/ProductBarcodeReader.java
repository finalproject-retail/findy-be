package com.princesses7.findy.shopping.product.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.external.mfds.MfdsBarcodeClient;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeItemResponse;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductBarcodeReader {

	private final ProductRepository productRepository;
	private final MfdsBarcodeClient mfdsBarcodeClient;

	public Long getProductIdByBarcode(String barcode) {
		validateBarcode(barcode);

		return productRepository.findByBarcodeAndIsDeletedFalse(barcode)
			.map(Product::getProductId)
			.orElseGet(() -> getExternalMatchedProductId(barcode));
	}

	private Long getExternalMatchedProductId(String barcode) {
		MfdsBarcodeResponse response = mfdsBarcodeClient.searchByBarcode(barcode);
		List<MfdsBarcodeItemResponse> items = response == null
			? List.of()
			: response.getItems();

		if (items.isEmpty()) {
			throw new ProductException(BARCODE_PRODUCT_NOT_FOUND);
		}

		// 식약처에는 존재하지만, 우리 매장 DB에는 아직 상품/가격/재고가 등록되지 않은 상태
		// 장바구니/쇼핑리스트 처리는 내부 Product + Inventory 기준으로만 진행한다.
		throw new ProductException(PRODUCT_NOT_FOUND);
	}

	private void validateBarcode(String barcode) {
		if (barcode == null || barcode.isBlank()) {
			throw new ProductException(INVALID_BARCODE);
		}
	}
}