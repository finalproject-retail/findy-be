package com.princesses7.findy.shopping.admin.product.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.admin.product.dto.response.AdminProductDetailResponse;
import com.princesses7.findy.shopping.admin.product.dto.response.AdminProductPageResponse;
import com.princesses7.findy.shopping.admin.product.dto.response.AdminProductResponse;
import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

	private static final int MAX_PAGE_SIZE = 100;
	private static final long DEFAULT_STORE_ID = 1L;

	private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
		"productId",
		"productName",
		"brandName",
		"categoryId",
		"originalPrice",
		"saleStatus",
		"createdAt",
		"updatedAt"
	);

	private final ProductRepository productRepository;
	private final InventoryRepository inventoryRepository;

	public AdminProductPageResponse getProducts(
		String keyword,
		Long categoryId,
		SaleStatus saleStatus,
		int page,
		int size,
		String sortBy,
		String direction
	) {
		validatePageRequest(page, size);

		Pageable pageable = PageRequest.of(
			page,
			size,
			createSort(sortBy, direction)
		);

		String normalizedKeyword = normalizeKeyword(keyword);

		Page<Product> products = findAdminProducts(
			normalizedKeyword,
			categoryId,
			saleStatus,
			pageable
		);

		return AdminProductPageResponse.from(toAdminProductResponsePage(products, pageable));
	}

	private Page<Product> findAdminProducts(
		String keyword,
		Long categoryId,
		SaleStatus saleStatus,
		Pageable pageable
	) {
		if (keyword == null) {
			return productRepository.findAdminProductsWithoutKeyword(
				categoryId,
				saleStatus,
				pageable
			);
		}

		return productRepository.findAdminProductsWithKeyword(
			keyword,
			categoryId,
			saleStatus,
			pageable
		);
	}

	public AdminProductDetailResponse getProductDetail(Long productId) {
		Product product = productRepository.findByProductIdAndDeletedAtIsNull(productId)
			.orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

		Inventory inventory = inventoryRepository.findByProductProductIdAndStoreId(productId, DEFAULT_STORE_ID)
			.orElse(null);

		return AdminProductDetailResponse.from(product, inventory);
	}

	private Page<AdminProductResponse> toAdminProductResponsePage(
		Page<Product> products,
		Pageable pageable
	) {
		List<AdminProductResponse> responses = toAdminProductResponses(products.getContent());

		return new PageImpl<>(
			responses,
			pageable,
			products.getTotalElements()
		);
	}

	private List<AdminProductResponse> toAdminProductResponses(List<Product> products) {
		if (products.isEmpty()) {
			return List.of();
		}

		List<Long> productIds = products.stream()
			.map(Product::getProductId)
			.toList();

		Map<Long, Inventory> inventoryMap = inventoryRepository.findAllByProductIdsAndStoreId(
				productIds,
				DEFAULT_STORE_ID
			)
			.stream()
			.collect(Collectors.toMap(
				inventory -> inventory.getProduct().getProductId(),
				Function.identity(),
				(existingInventory, replacementInventory) -> existingInventory
			));

		return products.stream()
			.map(product -> AdminProductResponse.from(
				product,
				inventoryMap.get(product.getProductId())
			))
			.toList();
	}

	private void validatePageRequest(int page, int size) {
		if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
			throw new ProductException(INVALID_INPUT_VALUE);
		}
	}

	private Sort createSort(String sortBy, String direction) {
		String sortProperty = sortBy == null || sortBy.isBlank()
			? "createdAt"
			: sortBy;

		if (!ALLOWED_SORT_PROPERTIES.contains(sortProperty)) {
			throw new ProductException(INVALID_SORT_TYPE);
		}

		return Sort.by(parseDirection(direction), sortProperty);
	}

	private Sort.Direction parseDirection(String direction) {
		if (direction == null || direction.isBlank()) {
			return Sort.Direction.DESC;
		}

		if ("asc".equalsIgnoreCase(direction)) {
			return Sort.Direction.ASC;
		}

		if ("desc".equalsIgnoreCase(direction)) {
			return Sort.Direction.DESC;
		}

		throw new ProductException(INVALID_SORT_TYPE);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}

		return keyword.trim();
	}
}