package com.princesses7.findy.shopping.recentview.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;
import com.princesses7.findy.shopping.recentview.dto.response.RecentViewProductListResponse;
import com.princesses7.findy.shopping.recentview.dto.response.RecentViewProductResponse;
import com.princesses7.findy.shopping.recentview.entity.RecentViewProduct;
import com.princesses7.findy.shopping.recentview.repository.RecentViewProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentViewProductService {

	private static final long DEFAULT_STORE_ID = 1L;
	private static final int MAX_RECENT_VIEW_SIZE = 50;

	private final RecentViewProductRepository recentViewProductRepository;
	private final ProductRepository productRepository;
	private final InventoryRepository inventoryRepository;

	@Transactional
	public void recordRecentView(Long userId, Long productId) {
		validateRequiredId(userId);
		validateRequiredId(productId);

		Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
			.orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

		recentViewProductRepository.findByUserIdAndProductProductId(userId, productId)
			.ifPresentOrElse(
				RecentViewProduct::refreshViewedAt,
				() -> recentViewProductRepository.save(RecentViewProduct.create(userId, product))
			);
	}

	public RecentViewProductListResponse getRecentViewProducts(Long userId, int limit) {
		validateRequiredId(userId);
		validateLimit(limit);

		List<RecentViewProduct> recentViewProducts = recentViewProductRepository.findRecentViewProducts(
			userId,
			PageRequest.of(0, limit)
		);

		if (recentViewProducts.isEmpty()) {
			return RecentViewProductListResponse.from(List.of());
		}

		Map<Long, Inventory> inventoryMap = getInventoryMap(recentViewProducts);

		List<RecentViewProductResponse> responses = recentViewProducts.stream()
			.map(recentViewProduct -> RecentViewProductResponse.from(
				recentViewProduct,
				inventoryMap.get(recentViewProduct.getProduct().getProductId())
			))
			.toList();

		return RecentViewProductListResponse.from(responses);
	}

	private Map<Long, Inventory> getInventoryMap(List<RecentViewProduct> recentViewProducts) {
		List<Long> productIds = recentViewProducts.stream()
			.map(recentViewProduct -> recentViewProduct.getProduct().getProductId())
			.toList();

		return inventoryRepository.findAllByProductIdsAndStoreId(productIds, DEFAULT_STORE_ID)
			.stream()
			.collect(Collectors.toMap(
				inventory -> inventory.getProduct().getProductId(),
				Function.identity(),
				(existingInventory, replacementInventory) -> existingInventory
			));
	}

	private void validateRequiredId(Long id) {
		if (id == null || id < 1) {
			throw new ProductException(INVALID_INPUT_VALUE);
		}
	}

	private void validateLimit(int limit) {
		if (limit < 1 || limit > MAX_RECENT_VIEW_SIZE) {
			throw new ProductException(INVALID_INPUT_VALUE);
		}
	}
}