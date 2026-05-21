package com.princesses7.findy.shopping.product.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.Collection;
import java.util.Map;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSummaryReader {

	private static final Long DEFAULT_STORE_ID = 1L;

	private final ProductRepository productRepository;
	private final InventoryRepository inventoryRepository;

	public Map<Long, ProductSummaryResponse> getProductSummaryMap(Collection<Long> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, Product> productById = productRepository
			.findAllByProductIdInAndIsDeletedFalse(productIds)
			.stream()
			.collect(Collectors.toMap(
				Product::getProductId,
				Function.identity()
			));

		Map<Long, Inventory> inventoryByProductId = inventoryRepository
			.findAllByProductIdsAndStoreId(productIds, DEFAULT_STORE_ID)
			.stream()
			.collect(Collectors.toMap(
				Inventory::getProductId,
				Function.identity()
			));

		return productIds.stream()
			.distinct()
			.collect(Collectors.toMap(
				Function.identity(),
				productId -> ProductSummaryResponse.from(
					getProduct(productById, productId),
					inventoryByProductId.get(productId)
				)
			));
	}

	public void validatePurchasable(Long productId, int quantity) {
		Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
			.orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

		if (product.getSaleStatus() != SaleStatus.ON_SALE) {
			throw new ProductException(INVALID_PRODUCT_STATUS);
		}

		Inventory inventory = inventoryRepository
			.findByProductProductIdAndStoreId(productId, DEFAULT_STORE_ID)
			.orElseThrow(() -> new ProductException(PRODUCT_STOCK_NOT_FOUND));

		if (inventory.getStockStatus() == StockStatus.OUT_OF_STOCK
			|| inventory.getStockQuantity() < quantity) {
			throw new ProductException(INVENTORY_INSUFFICIENT_STOCK);
		}
	}

	private Product getProduct(Map<Long, Product> productById, Long productId) {
		Product product = productById.get(productId);

		if (product == null) {
			throw new ProductException(PRODUCT_NOT_FOUND);
		}

		return product;
	}
}