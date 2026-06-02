package com.princesses7.findy.shopping.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.external.naver.NaverProductMapper;
import com.princesses7.findy.shopping.external.naver.NaverShoppingClient;
import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingItemResponse;
import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingResponse;
import com.princesses7.findy.shopping.inventory.service.InventoryService;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.dto.response.ProductImportResultResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverProductImportService {

	private static final String EXTERNAL_SOURCE = "NAVER";

	private final NaverShoppingClient naverShoppingClient;
	private final NaverProductMapper naverProductMapper;
	private final ProductRepository productRepository;
	private final InventoryService inventoryService;

	@Transactional
	public ProductImportResultResponse importByKeyword(String keyword, int display) {
		NaverShoppingResponse response = naverShoppingClient.search(keyword, display, 1);
		List<NaverShoppingItemResponse> items = response.items();

		int importedCount = 0;
		int skippedCount = 0;

		for (NaverShoppingItemResponse item : items) {
			if (isInvalid(item) || isDuplicated(item)) {
				skippedCount++;
				continue;
			}

			try {
				ProductImportCommand command = naverProductMapper.toCommand(item);
				Product product = productRepository.save(Product.create(command));
				inventoryService.createDefaultInventory(product);

				importedCount++;
			} catch (ProductException exception) {
				log.warn("상품 import를 건너뜁니다. productId={}, title={}", item.productId(), item.title(), exception);
				skippedCount++;
			}
		}

		return new ProductImportResultResponse(importedCount, skippedCount);
	}

	private boolean isInvalid(NaverShoppingItemResponse item) {
		return item.productId() == null || item.productId().isBlank()
			|| item.title() == null || item.title().isBlank()
			|| item.lprice() == null || item.lprice().isBlank();
	}

	private boolean isDuplicated(NaverShoppingItemResponse item) {
		return productRepository.existsByExternalSourceAndExternalProductIdAndDeletedAtIsNull(
			EXTERNAL_SOURCE,
			item.productId()
		);
	}
}