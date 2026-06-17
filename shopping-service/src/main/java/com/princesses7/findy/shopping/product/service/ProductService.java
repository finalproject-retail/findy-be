package com.princesses7.findy.shopping.product.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDateTime;
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

import com.princesses7.findy.shopping.analytics.event.ProductViewSource;
import com.princesses7.findy.shopping.analytics.publisher.ShoppingAnalyticsEventService;
import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.product.dto.response.ProductDetailResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductLocationResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductPageResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductStockResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.repository.PromotionProductRepository;
import com.princesses7.findy.shopping.search.service.SearchKeywordRankingService;
import com.princesses7.findy.shopping.store.StoreIdSupport;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private static final int MAX_PAGE_SIZE = 100;
	private static final int MAX_SECTION_SIZE = 30;
	private static final int SECTION_CANDIDATE_MULTIPLIER = 3;
	private static final String POPULAR_SORT = "popular";
	private static final int MIN_VISIBLE_PRICE = 0;
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
	private final ShoppingAnalyticsEventService shoppingAnalyticsEventService;
	private final PromotionProductRepository promotionProductRepository;

	public ProductPageResponse getProducts(
		Long categoryId,
		String keyword,
		int page,
		int size,
		String sortBy,
		String direction,
		long storeId
	) {
		validatePageRequest(page, size);

		String normalizedKeyword = normalizeKeyword(keyword);

		if (normalizedKeyword != null) {
			searchKeywordRankingService.record(normalizedKeyword);
		}

		if (isPopularSort(sortBy)) {
			return getProductsByPopularRanking(
				categoryId,
				normalizedKeyword,
				page,
				size,
				storeId
			);
		}

		Sort sort = createSort(sortBy, direction);
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Product> products = findProducts(categoryId, normalizedKeyword, pageable);
		Page<ProductResponse> responsePage = toProductResponsePage(products, pageable, storeId);

		return ProductPageResponse.from(responsePage);
	}

	public List<ProductResponse> getNewProducts(int size, long storeId) {
		validateSectionSize(size);

		Pageable pageable = PageRequest.of(0, size);

		return toProductResponses(
			productRepository.findNewDisplayableProducts(
				MIN_VISIBLE_PRICE,
				pageable
			).getContent(),
			storeId
		);
	}

	public List<ProductResponse> getPopularProducts(int size, long storeId) {
		validateSectionSize(size);

		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		List<Long> productIds = productRankingService.getAllPopularProductIds();

		if (productIds.isEmpty()) {
			return getMartRecommendedProducts(size, resolvedStoreId);
		}

		List<Product> popularProducts = findProductsByRanking(
			productIds,
			null,
			null,
			resolvedStoreId
		)
			.stream()
			.limit(size)
			.toList();

		if (popularProducts.isEmpty()) {
			return getMartRecommendedProducts(size, resolvedStoreId);
		}

		return toProductResponses(popularProducts, resolvedStoreId);
	}

	public ProductDetailResponse getProductDetail(
		Long productId,
		long storeId,
		Long userId,
		String viewSource,
		Long promotionId,
		Long pinGridId
	) {
		Product product = findActiveProduct(productId);

		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		Inventory inventory = inventoryRepository.findByProductProductIdAndStoreId(
				productId,
				resolvedStoreId
			)
			.orElse(null);

		PromotionProduct promotionProduct = findBestPromotionProduct(productId);

		productRankingService.recordView(productId);
		publishProductViewed(userId, productId, viewSource, promotionId, pinGridId);

		return ProductDetailResponse.from(product, inventory, promotionProduct);
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> getMartRecommendedProducts(
		int size,
		long storeId
	) {
		validateSectionSize(size);

		int resolvedSize = Math.min(size, MAX_SECTION_SIZE);
		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		List<Long> productIds = productRankingService.getFindyMartRecommendedProductIds(
			resolvedSize * SECTION_CANDIDATE_MULTIPLIER
		);

		if (!productIds.isEmpty()) {
			List<Product> recommendedProducts = findProductsByRanking(
				productIds,
				null,
				null,
				resolvedStoreId
			)
				.stream()
				.limit(resolvedSize)
				.toList();

			if (!recommendedProducts.isEmpty()) {
				return toProductResponses(recommendedProducts, resolvedStoreId);
			}
		}

		List<Product> fallbackProducts = productRepository.findMartRecommendedProducts(
			resolvedStoreId,
			PageRequest.of(0, resolvedSize)
		);

		return toProductResponses(fallbackProducts, resolvedStoreId);
	}

	private void publishProductViewed(
		Long userId,
		Long productId,
		String viewSource,
		Long promotionId,
		Long pinGridId
	) {
		ProductViewSource productViewSource = ProductViewSource.fromNullable(viewSource);

		if (productViewSource == ProductViewSource.MAP_PROMOTION) {
			shoppingAnalyticsEventService.publishMapPromotionProductViewed(
				userId,
				productId,
				promotionId,
				pinGridId
			);
			return;
		}

		shoppingAnalyticsEventService.publishProductViewed(userId, productId);
	}

	public ProductLocationResponse getProductLocation(Long productId, long storeId) {
		Product product = findActiveProduct(productId);

		if (product.getGridId() == null) {
			throw new ProductException(PRODUCT_LOCATION_NOT_FOUND);
		}

		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		return ProductLocationResponse.from(product, resolvedStoreId);
	}

	public ProductStockResponse getProductStock(Long productId, long storeId) {
		Product product = findActiveProduct(productId);

		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		Inventory inventory = inventoryRepository.findByProductProductIdAndStoreId(
				productId,
				resolvedStoreId
			)
			.orElseThrow(() -> new ProductException(PRODUCT_STOCK_NOT_FOUND));

		return ProductStockResponse.from(product, inventory);
	}

	private Product findActiveProduct(Long productId) {
		return productRepository.findByProductIdAndDeletedAtIsNullAndOriginalPriceGreaterThan(
				productId,
				MIN_VISIBLE_PRICE
			)
			.orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
	}

	private Page<Product> findProducts(
		Long categoryId,
		String keyword,
		Pageable pageable
	) {
		if (categoryId != null && keyword != null) {
			return productRepository.findByCategoryIdAndProductNameContainingIgnoreCaseAndDeletedAtIsNullAndOriginalPriceGreaterThan(
				categoryId,
				keyword,
				MIN_VISIBLE_PRICE,
				pageable
			);
		}

		if (categoryId != null) {
			return productRepository.findByCategoryIdAndDeletedAtIsNullAndOriginalPriceGreaterThan(
				categoryId,
				MIN_VISIBLE_PRICE,
				pageable
			);
		}

		if (keyword != null) {
			return productRepository.findByProductNameContainingIgnoreCaseAndDeletedAtIsNullAndOriginalPriceGreaterThan(
				keyword,
				MIN_VISIBLE_PRICE,
				pageable
			);
		}

		return productRepository.findByDeletedAtIsNullAndOriginalPriceGreaterThan(
			MIN_VISIBLE_PRICE,
			pageable
		);
	}

	private List<Product> findProductsByRanking(
		List<Long> productIds,
		Long categoryId,
		String keyword,
		long storeId
	) {
		if (productIds.isEmpty()) {
			return List.of();
		}

		String keywordForQuery = normalizeKeywordForQuery(keyword);

		Map<Long, Product> productMap = productRepository.findPopularProductsByRedisIds(
				productIds,
				categoryId,
				keywordForQuery,
				storeId
			)
			.stream()
			.collect(Collectors.toMap(
				Product::getProductId,
				Function.identity(),
				(existingProduct, replacementProduct) -> existingProduct
			));

		return productIds.stream()
			.map(productMap::get)
			.filter(product -> product != null)
			.toList();
	}

	private Page<ProductResponse> toProductResponsePage(
		Page<Product> products,
		Pageable pageable,
		long storeId
	) {
		List<ProductResponse> responses = toProductResponses(products.getContent(), storeId);

		return new PageImpl<>(
			responses,
			pageable,
			products.getTotalElements()
		);
	}

	private List<ProductResponse> toProductResponses(List<Product> products, long storeId) {
		if (products.isEmpty()) {
			return List.of();
		}

		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		List<Long> productIds = products.stream()
			.map(Product::getProductId)
			.toList();

		Map<Long, Inventory> inventoryMap = inventoryRepository.findAllByProductIdsAndStoreId(
				productIds,
				resolvedStoreId
			)
			.stream()
			.collect(Collectors.toMap(
				inventory -> inventory.getProduct().getProductId(),
				Function.identity(),
				(existingInventory, replacementInventory) -> existingInventory
			));

		Map<Long, PromotionProduct> promotionProductMap = promotionProductRepository
			.findApplicablePromotionProductsByProductIds(
				productIds,
				PromotionStatus.ENDED,
				LocalDateTime.now()
			)
			.stream()
			.collect(Collectors.toMap(
				PromotionProduct::getProductId,
				Function.identity(),
				this::selectBetterPromotionProduct
			));

		return products.stream()
			.map(product -> ProductResponse.from(
				product,
				inventoryMap.get(product.getProductId()),
				promotionProductMap.get(product.getProductId())
			))
			.toList();
	}

	private PromotionProduct findBestPromotionProduct(Long productId) {
		return promotionProductRepository
			.findApplicablePromotionProductsByProductIds(
				List.of(productId),
				PromotionStatus.ENDED,
				LocalDateTime.now()
			)
			.stream()
			.reduce(this::selectBetterPromotionProduct)
			.orElse(null);
	}

	private PromotionProduct selectBetterPromotionProduct(
		PromotionProduct current,
		PromotionProduct candidate
	) {
		Integer currentPrice = current.getPromotionPrice();
		Integer candidatePrice = candidate.getPromotionPrice();

		if (currentPrice == null) {
			return candidate;
		}

		if (candidatePrice == null) {
			return current;
		}

		return candidatePrice < currentPrice ? candidate : current;
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

	private String normalizeKeywordForQuery(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return "";
		}

		return keyword.trim();
	}

	private ProductPageResponse getProductsByPopularRanking(
		Long categoryId,
		String keyword,
		int page,
		int size,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		List<Long> rankedProductIds = productRankingService.getAllPopularProductIds();

		if (rankedProductIds.isEmpty()) {
			Pageable fallbackPageable = PageRequest.of(
				page,
				size,
				Sort.by(Sort.Direction.DESC, "createdAt")
			);

			Page<Product> fallbackProducts = findProducts(categoryId, keyword, fallbackPageable);
			Page<ProductResponse> fallbackResponsePage = toProductResponsePage(
				fallbackProducts,
				fallbackPageable,
				resolvedStoreId
			);

			return ProductPageResponse.from(fallbackResponsePage);
		}

		String keywordForQuery = normalizeKeywordForQuery(keyword);

		List<Product> filteredProducts = productRepository.findPopularProductsByRedisIds(
			rankedProductIds,
			categoryId,
			keywordForQuery,
			resolvedStoreId
		);

		Map<Long, Product> productMap = filteredProducts.stream()
			.collect(Collectors.toMap(
				Product::getProductId,
				Function.identity(),
				(existingProduct, replacementProduct) -> existingProduct
			));

		List<Product> rankedProducts = rankedProductIds.stream()
			.map(productMap::get)
			.filter(product -> product != null)
			.toList();

		int fromIndex = page * size;
		int toIndex = Math.min(fromIndex + size, rankedProducts.size());

		List<Product> pageProducts = fromIndex >= rankedProducts.size()
			? List.of()
			: rankedProducts.subList(fromIndex, toIndex);

		Pageable pageable = PageRequest.of(page, size);

		Page<ProductResponse> responsePage = new PageImpl<>(
			toProductResponses(pageProducts, resolvedStoreId),
			pageable,
			rankedProducts.size()
		);

		return ProductPageResponse.from(responsePage);
	}

	private boolean isPopularSort(String sortBy) {
		return POPULAR_SORT.equalsIgnoreCase(sortBy);
	}
}