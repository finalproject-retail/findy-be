package com.princesses7.findy.shopping.product.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.entity.StockStatus;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;
import com.princesses7.findy.shopping.store.StoreIdSupport;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSummaryReader {

	private final ProductRepository productRepository;
	private final InventoryRepository inventoryRepository;

	public Map<Long, ProductSummaryResponse> getProductSummaryMap(
		Collection<Long> productIds,
		long storeId
	) {
		if (productIds == null || productIds.isEmpty()) {
			return Map.of();
		}

		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		Map<Long, Product> productById = productRepository
			.findAllVisibleByProductIdIn(productIds)
			.stream()
			.collect(Collectors.toMap(
				Product::getProductId,
				Function.identity()
			));

		Map<Long, Inventory> inventoryByProductId = inventoryRepository
			.findAllByProductIdsAndStoreId(productIds, resolvedStoreId)
			.stream()
			.collect(Collectors.toMap(
				Inventory::getProductId,
				Function.identity()
			));

		return productIds.stream()
			.distinct()
			.map(productById::get)
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(
				Product::getProductId,
				product -> ProductSummaryResponse.from(
					product,
					inventoryByProductId.get(product.getProductId())
				),
				(existingProduct, replacementProduct) -> existingProduct
			));
	}

	public void validatePurchasable(Long productId, int quantity, long storeId) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		Product product = productRepository
			.findByProductIdAndDeletedAtIsNullAndOriginalPriceGreaterThan(productId, 0)
			.orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

		if (product.getSaleStatus() != SaleStatus.ON_SALE) {
			throw new ProductException(INVALID_PRODUCT_STATUS);
		}

		Inventory inventory = inventoryRepository
			.findByProductProductIdAndStoreId(productId, resolvedStoreId)
			.orElseThrow(() -> new ProductException(PRODUCT_STOCK_NOT_FOUND));

		if (inventory.getStockStatus() == StockStatus.OUT_OF_STOCK
			|| inventory.getStockQuantity() < quantity) {
			throw new ProductException(INVENTORY_INSUFFICIENT_STOCK);
		}
	}

	public List<ProductSummaryResponse> getExistingProductSummaries(
		Collection<Long> productIds,
		long storeId
	) {
		if (productIds == null || productIds.isEmpty()) {
			return List.of();
		}

		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		List<Long> distinctProductIds = productIds.stream()
			.filter(Objects::nonNull)
			.distinct()
			.toList();

		if (distinctProductIds.isEmpty()) {
			return List.of();
		}

		Map<Long, Product> productById = productRepository
			.findAllVisibleByProductIdIn(distinctProductIds)
			.stream()
			.collect(Collectors.toMap(
				Product::getProductId,
				Function.identity(),
				(existingProduct, replacementProduct) -> existingProduct
			));

		Map<Long, Inventory> inventoryByProductId = inventoryRepository
			.findAllByProductIdsAndStoreId(distinctProductIds, resolvedStoreId)
			.stream()
			.collect(Collectors.toMap(
				Inventory::getProductId,
				Function.identity(),
				(existingInventory, replacementInventory) -> existingInventory
			));

		return distinctProductIds.stream()
			.map(productById::get)
			.filter(Objects::nonNull)
			.map(product -> ProductSummaryResponse.from(
				product,
				inventoryByProductId.get(product.getProductId())
			))
			.toList();
	}
}
