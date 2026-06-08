package com.princesses7.findy.shopping.product.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.princesses7.findy.shopping.external.ai.CategoryClassifierClient;
import com.princesses7.findy.shopping.external.haccp.HaccpProductClient;
import com.princesses7.findy.shopping.external.haccp.HaccpProductMapper;
import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;
import com.princesses7.findy.shopping.inventory.service.InventoryService;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.dto.request.HaccpProductBulkImportRequest;
import com.princesses7.findy.shopping.product.dto.response.HaccpProductBulkImportItemResponse;
import com.princesses7.findy.shopping.product.dto.response.HaccpProductBulkImportResponse;
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

	private static final int DEFAULT_IMPORT_LIMIT = 10;
	private static final int DEFAULT_START_PAGE = 1;
	private static final int DEFAULT_NUM_OF_ROWS = 50;
	private static final int DEFAULT_MAX_PAGES = 1;
	private static final int MAX_NUM_OF_ROWS = 100;
	private static final int MAX_PAGES = 20;

	private final HaccpProductClient haccpProductClient;
	private final HaccpProductMapper haccpProductMapper;
	private final CategoryClassifierClient categoryClassifierClient;
	private final ProductRepository productRepository;
	private final InventoryService inventoryService;

	@Transactional
	public HaccpProductImportResponse importByProductName(String productName) {
		return importByProductName(productName, DEFAULT_IMPORT_LIMIT, false, true);
	}

	@Transactional
	public HaccpProductImportResponse importByProductName(
		String productName,
		int limit,
		boolean onlyBarcodeExists,
		boolean createIfMissing
	) {
		if (!StringUtils.hasText(productName)) {
			throw new ProductException(INVALID_SEARCH_KEYWORD);
		}

		List<HaccpProductItemResponse> haccpItems = haccpProductClient.searchByProductName(productName);

		if (haccpItems.isEmpty()) {
			throw new ProductException(HACCP_PRODUCT_NOT_FOUND);
		}

		return importItems(
			productName.trim(),
			haccpItems,
			limit,
			onlyBarcodeExists,
			createIfMissing
		);
	}

	@Transactional
	public HaccpProductBulkImportResponse bulkImport(HaccpProductBulkImportRequest request) {
		if (request == null || request.keywords() == null || request.keywords().isEmpty()) {
			throw new ProductException(INVALID_SEARCH_KEYWORD);
		}

		int createdCount = 0;
		int updatedCount = 0;
		int skippedCount = 0;

		List<HaccpProductBulkImportItemResponse> bulkItems = new ArrayList<>();

		for (String keyword : request.keywords()) {
			if (!StringUtils.hasText(keyword)) {
				skippedCount++;
				bulkItems.add(new HaccpProductBulkImportItemResponse(
					keyword,
					null,
					null,
					null,
					STATUS_SKIPPED,
					List.of("검색어가 비어 있습니다.")
				));
				continue;
			}

			String normalizedKeyword = keyword.trim();
			List<HaccpProductItemResponse> haccpItems = haccpProductClient.searchByProductName(normalizedKeyword);

			if (haccpItems.isEmpty()) {
				skippedCount++;
				bulkItems.add(new HaccpProductBulkImportItemResponse(
					normalizedKeyword,
					null,
					null,
					null,
					STATUS_SKIPPED,
					List.of("HACCP 조회 결과가 없습니다.")
				));
				continue;
			}

			HaccpProductImportResponse response = importItems(
				normalizedKeyword,
				haccpItems,
				request.resolvedLimitPerKeyword(),
				request.resolvedOnlyBarcodeExists(),
				request.resolvedCreateIfMissing()
			);

			createdCount += response.importedCount();
			updatedCount += response.updatedCount();
			skippedCount += response.skippedCount();

			for (HaccpProductImportItemResponse item : response.items()) {
				bulkItems.add(HaccpProductBulkImportItemResponse.from(normalizedKeyword, item));
			}
		}

		return new HaccpProductBulkImportResponse(
			request.keywords().size(),
			createdCount,
			updatedCount,
			skippedCount,
			bulkItems
		);
	}

	@Transactional
	public HaccpProductBulkImportResponse importAll(
		int startPage,
		int numOfRows,
		int maxPages,
		boolean onlyBarcodeExists
	) {
		int resolvedStartPage = resolveStartPage(startPage);
		int resolvedNumOfRows = resolveNumOfRows(numOfRows);
		int resolvedMaxPages = resolveMaxPages(maxPages);

		int processedPageCount = 0;
		int createdCount = 0;
		int updatedCount = 0;
		int skippedCount = 0;

		List<HaccpProductBulkImportItemResponse> bulkItems = new ArrayList<>();

		for (int page = resolvedStartPage; page < resolvedStartPage + resolvedMaxPages; page++) {
			List<HaccpProductItemResponse> haccpItems = haccpProductClient.getProducts(
				page,
				resolvedNumOfRows
			);

			if (haccpItems.isEmpty()) {
				break;
			}

			processedPageCount++;

			String pageKeyword = "page:" + page;

			HaccpProductImportResponse response = importItems(
				pageKeyword,
				haccpItems,
				haccpItems.size(),
				onlyBarcodeExists,
				true
			);

			createdCount += response.importedCount();
			updatedCount += response.updatedCount();
			skippedCount += response.skippedCount();

			for (HaccpProductImportItemResponse item : response.items()) {
				bulkItems.add(HaccpProductBulkImportItemResponse.from(pageKeyword, item));
			}

			if (haccpItems.size() < resolvedNumOfRows) {
				break;
			}
		}

		return new HaccpProductBulkImportResponse(
			processedPageCount,
			createdCount,
			updatedCount,
			skippedCount,
			bulkItems
		);
	}

	private HaccpProductImportResponse importItems(
		String keyword,
		List<HaccpProductItemResponse> haccpItems,
		int limit,
		boolean onlyBarcodeExists,
		boolean createIfMissing
	) {
		int importedCount = 0;
		int updatedCount = 0;
		int skippedCount = 0;

		List<HaccpProductImportItemResponse> resultItems = new ArrayList<>();

		for (HaccpProductItemResponse haccpItem : haccpItems.stream().limit(resolveLimit(limit)).toList()) {
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

			if (onlyBarcodeExists && !hasUsableBarcode(haccpItem.barcode())) {
				skippedCount++;
				resultItems.add(new HaccpProductImportItemResponse(
					null,
					haccpItem.productName(),
					haccpItem.barcode(),
					STATUS_SKIPPED,
					List.of("바코드가 없어 자동 등록에서 제외했습니다.")
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

			if (!createIfMissing) {
				skippedCount++;
				resultItems.add(new HaccpProductImportItemResponse(
					null,
					enrichmentCommand.productName(),
					enrichmentCommand.barcode(),
					STATUS_SKIPPED,
					List.of("신규 상품 생성 옵션이 꺼져 있어 제외했습니다.")
				));
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

	private boolean hasUsableBarcode(String barcode) {
		if (!StringUtils.hasText(barcode)) {
			return false;
		}

		String trimmedBarcode = barcode.trim();

		return !"알수없음".equals(trimmedBarcode)
			&& !"알 수 없음".equals(trimmedBarcode)
			&& !"UNKNOWN".equalsIgnoreCase(trimmedBarcode);
	}

	private ProductCategoryClassificationResponse classifyCategory(HaccpProductItemResponse haccpItem) {
		return categoryClassifierClient.classify(
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
			Optional<Product> product = productRepository.findByBarcodeAndDeletedAtIsNull(command.barcode());

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

		return !productRepository.existsByBarcodeAndDeletedAtIsNull(barcode);
	}

	private boolean isBarcodeDuplicated(String barcode) {
		return StringUtils.hasText(barcode)
			&& productRepository.existsByBarcodeAndDeletedAtIsNull(barcode);
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

	private int resolveLimit(int limit) {
		if (limit <= 0) {
			return DEFAULT_IMPORT_LIMIT;
		}

		return limit;
	}

	private int resolveStartPage(int startPage) {
		if (startPage <= 0) {
			return DEFAULT_START_PAGE;
		}

		return startPage;
	}

	private int resolveNumOfRows(int numOfRows) {
		if (numOfRows <= 0) {
			return DEFAULT_NUM_OF_ROWS;
		}

		return Math.min(numOfRows, MAX_NUM_OF_ROWS);
	}

	private int resolveMaxPages(int maxPages) {
		if (maxPages <= 0) {
			return DEFAULT_MAX_PAGES;
		}

		return Math.min(maxPages, MAX_PAGES);
	}
}