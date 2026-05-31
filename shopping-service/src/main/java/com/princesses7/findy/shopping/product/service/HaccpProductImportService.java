package com.princesses7.findy.shopping.product.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.princesses7.findy.shopping.external.haccp.HaccpProductClient;
import com.princesses7.findy.shopping.external.haccp.HaccpProductMapper;
import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;
import com.princesses7.findy.shopping.external.openai.OpenAiCategoryClassifierClient;
import com.princesses7.findy.shopping.inventory.service.InventoryService;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.dto.response.HaccpProductImportItemResponse;
import com.princesses7.findy.shopping.product.dto.response.HaccpProductImportResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HaccpProductImportService {

	private static final String STATUS_CREATED = "CREATED";
	private static final String STATUS_UPDATED = "UPDATED";
	private static final String STATUS_UNCHANGED = "UNCHANGED";
	private static final String STATUS_SKIPPED = "SKIPPED";

	private final HaccpProductClient haccpProductClient;
	private final HaccpProductMapper haccpProductMapper;
	private final OpenAiCategoryClassifierClient openAiCategoryClassifierClient;
	private final ProductRepository productRepository;
	private final InventoryService inventoryService;

	@Transactional
	public HaccpProductImportResponse importByProductName(String productName) {
		if (!StringUtils.hasText(productName)) {
			throw new ProductException(INVALID_SEARCH_KEYWORD);
		}

		List<HaccpProductItemResponse> haccpItems = haccpProductClient.searchByProductName(productName);

		if (haccpItems.isEmpty()) {
			throw new ProductException(HACCP_PRODUCT_NOT_FOUND);
		}

		int importedCount = 0;
		int updatedCount = 0;
		int skippedCount = 0;

		List<HaccpProductImportItemResponse> resultItems = new ArrayList<>();

		for (HaccpProductItemResponse haccpItem : haccpItems) {
			if (isInvalid(haccpItem)) {
				skippedCount++;
				resultItems.add(new HaccpProductImportItemResponse(
					null,
					null,
					null,
					STATUS_SKIPPED,
					List.of("HACCP 상품명이 비어 있습니다.")
				));
				continue;
			}

			ProductImportCommand enrichmentCommand = haccpProductMapper.toEnrichmentCommand(haccpItem);
			Optional<Product> existingProduct = findExistingProduct(enrichmentCommand);

			if (existingProduct.isPresent()) {
				Product product = existingProduct.get();
				boolean canApplyBarcode = canApplyBarcode(product, enrichmentCommand.barcode());
				List<String> updatedFields = product.enrichMissingFields(enrichmentCommand, canApplyBarcode);

				if (updatedFields.isEmpty()) {
					resultItems.add(toResponseItem(product, STATUS_UNCHANGED, updatedFields));
				} else {
					updatedCount++;
					resultItems.add(toResponseItem(product, STATUS_UPDATED, updatedFields));
				}

				continue;
			}

			ProductCategoryClassificationResponse classification = classifyCategory(haccpItem);

			if (!classification.isClassified()) {
				skippedCount++;
				resultItems.add(new HaccpProductImportItemResponse(
					null,
					haccpItem.productName(),
					haccpItem.barcode(),
					STATUS_SKIPPED,
					List.of(classification.reason())
				));
				continue;
			}

			ProductImportCommand createCommand = haccpProductMapper.toCreateCommand(haccpItem, classification);

			if (isBarcodeDuplicated(createCommand.barcode())) {
				skippedCount++;
				resultItems.add(new HaccpProductImportItemResponse(
					null,
					createCommand.productName(),
					createCommand.barcode(),
					STATUS_SKIPPED,
					List.of("이미 다른 상품에 등록된 바코드입니다.")
				));
				continue;
			}

			Product product = productRepository.save(Product.create(createCommand));
			inventoryService.createDefaultInventory(product);

			importedCount++;
			resultItems.add(toResponseItem(product, STATUS_CREATED, List.of()));
		}

		return new HaccpProductImportResponse(
			importedCount,
			updatedCount,
			skippedCount,
			resultItems
		);
	}

	private ProductCategoryClassificationResponse classifyCategory(HaccpProductItemResponse haccpItem) {
		return openAiCategoryClassifierClient.classify(
			haccpItem.productName(),
			haccpProductMapper.extractBrandName(haccpItem),
			haccpItem.productKind()
		);
	}

	private boolean isInvalid(HaccpProductItemResponse haccpItem) {
		return haccpItem == null || !StringUtils.hasText(haccpItem.productName());
	}

	private Optional<Product> findExistingProduct(ProductImportCommand command) {
		if (StringUtils.hasText(command.barcode())) {
			Optional<Product> product = productRepository.findByBarcodeAndIsDeletedFalse(command.barcode());

			if (product.isPresent()) {
				return product;
			}
		}

		String normalizedProductName = normalizeProductName(command.productName());

		if (!StringUtils.hasText(normalizedProductName)) {
			return Optional.empty();
		}

		return productRepository.findAllByNormalizedProductName(normalizedProductName)
			.stream()
			.findFirst();
	}

	private boolean canApplyBarcode(Product product, String barcode) {
		if (!StringUtils.hasText(barcode)) {
			return false;
		}

		if (barcode.equals(product.getBarcode())) {
			return true;
		}

		return !productRepository.existsByBarcodeAndIsDeletedFalse(barcode);
	}

	private boolean isBarcodeDuplicated(String barcode) {
		return StringUtils.hasText(barcode)
			&& productRepository.existsByBarcodeAndIsDeletedFalse(barcode);
	}

	private String normalizeProductName(String productName) {
		if (!StringUtils.hasText(productName)) {
			return "";
		}

		return productName.toLowerCase()
			.replaceAll("\\s+", "")
			.trim();
	}

	private HaccpProductImportItemResponse toResponseItem(
		Product product,
		String importStatus,
		List<String> updatedFields
	) {
		return new HaccpProductImportItemResponse(
			product.getProductId(),
			product.getProductName(),
			product.getBarcode(),
			importStatus,
			updatedFields
		);
	}
}