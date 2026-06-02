package com.princesses7.findy.shopping.product.service;

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

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.product.dto.response.ProductDetailResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductPageResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;
import com.princesses7.findy.shopping.search.service.SearchKeywordRankingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private static final int MAX_PAGE_SIZE = 100;
	private static final int MAX_SECTION_SIZE = 30;
	private static final int SECTION_CANDIDATE_MULTIPLIER = 3;
	private static final long DEFAULT_STORE_ID = 1L;

	private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
		"productId",
		"productName",
		"originalPrice",
		"createdAt"
	);

	private final ProductRepository productRepository;
	private final InventoryRepository inventoryRepository;
	private final SearchKeywordRankingService searchKeywordRankingService;
	private final ProductRankingService productRankingService;

	public ProductPageResponse getProducts(
		Long categoryId,
		String keyword,
		int page,
		int size,
		String sortBy,
		String direction
	) {
		validatePageRequest(page, size);

		Sort sort = createSort(sortBy, direction);
		Pageable pageable = PageRequest.of(page, size, sort);

		String normalizedKeyword = normalizeKeyword(keyword);

		if (normalizedKeyword != null) {
			searchKeywordRankingService.record(normalizedKeyword);
		}

		Page<Product> products = findProducts(categoryId, normalizedKeyword, pageable);
		Page<ProductResponse> responsePage = toProductResponsePage(products, pageable);

		return ProductPageResponse.from(responsePage);
	}

	public List<ProductResponse> getNewProducts(int size) {
		validateSectionSize(size);

		Pageable pageable = PageRequest.of(
			0,
			size,
			Sort.by(Sort.Direction.DESC, "createdAt")
		);

		return toProductResponses(
			productRepository.findByDeletedAtIsNull(pageable).getContent()
		);
	}

	public List<ProductResponse> getPopularProducts(int size) {
		validateSectionSize(size);

		List<Long> productIds = productRankingService.getPopularProductIds(
			size * SECTION_CANDIDATE_MULTIPLIER
		);

		if (productIds.isEmpty()) {
			return getMartRecommendedProducts(size);
		}

		List<Product> popularProducts = findProductsByRanking(productIds, size);

		if (popularProducts.isEmpty()) {
			return getMartRecommendedProducts(size);
		}

		return toProductResponses(popularProducts);
	}

	public List<ProductResponse> getMartRecommendedProducts(int size) {
		validateSectionSize(size);

		return toProductResponses(
			productRepository.findMartRecommendedProducts(PageRequest.of(0, size))
		);
	}

	public ProductDetailResponse getProductDetail(Long productId) {
		Product product = productRepository.findByProductIdAndDeletedAtIsNull(productId)
			.orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

		Inventory inventory = inventoryRepository.findByProductProductIdAndStoreId(productId, DEFAULT_STORE_ID)
			.orElse(null);

		productRankingService.recordView(productId);

		return ProductDetailResponse.from(product, inventory);
	}

	private Page<Product> findProducts(
		Long categoryId,
		String keyword,
		Pageable pageable
	) {
		if (categoryId != null && keyword != null) {
			return productRepository.findByCategoryIdAndProductNameContainingIgnoreCaseAndDeletedAtIsNull(
				categoryId,
				keyword,
				pageable
			);
		}

		if (categoryId != null) {
			return productRepository.findByCategoryIdAndDeletedAtIsNull(categoryId, pageable);
		}

		if (keyword != null) {
			return productRepository.findByProductNameContainingIgnoreCaseAndDeletedAtIsNull(
				keyword,
				pageable
			);
		}

		return productRepository.findByDeletedAtIsNull(pageable);
	}

	private List<Product> findProductsByRanking(
		List<Long> productIds,
		int size
	) {
		Map<Long, Product> productMap = productRepository.findAllByProductIdInAndDeletedAtIsNull(productIds)
			.stream()
			.collect(Collectors.toMap(
				Product::getProductId,
				Function.identity(),
				(existingProduct, replacementProduct) -> existingProduct
			));

		return productIds.stream()
			.map(productMap::get)
			.filter(product -> product != null)
			.limit(size)
			.toList();
	}

	private Page<ProductResponse> toProductResponsePage(
		Page<Product> products,
		Pageable pageable
	) {
		List<ProductResponse> responses = toProductResponses(products.getContent());

		return new PageImpl<>(
			responses,
			pageable,
			products.getTotalElements()
		);
	}

	private List<ProductResponse> toProductResponses(List<Product> products) {
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
			.map(product -> ProductResponse.from(
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

	private void validateSectionSize(int size) {
		if (size < 1 || size > MAX_SECTION_SIZE) {
			throw new ProductException(INVALID_INPUT_VALUE);
		}
	}

	private Sort createSort(String sortBy, String direction) {
		// TODO: 인기순 정렬은 Redis 랭킹 데이터 또는 상품 조회 로그 연동 시 별도 구현
		String sortProperty = sortBy == null || sortBy.isBlank()
			? "createdAt"
			: sortBy;

		if (!ALLOWED_SORT_PROPERTIES.contains(sortProperty)) {
			throw new ProductException(INVALID_SORT_TYPE);
		}

		Sort.Direction sortDirection = parseDirection(direction);

		return Sort.by(sortDirection, sortProperty);
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