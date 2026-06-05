package com.princesses7.findy.recommendation.recommendation.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.princesses7.findy.recommendation.external.shopping.dto.FrequentPurchaseProductResponse;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;

public record PurchaseHistoryContext(
	List<FrequentPurchaseProductResponse> products,
	Map<Long, Long> purchaseQuantityByProductId,
	Map<Long, Long> purchaseQuantityByCategoryId,
	long maxProductPurchaseQuantity,
	long maxCategoryPurchaseQuantity
) {

	public static PurchaseHistoryContext from(List<FrequentPurchaseProductResponse> products) {
		List<FrequentPurchaseProductResponse> safeProducts = products == null ? List.of() : products;

		Map<Long, Long> productQuantityMap = safeProducts.stream()
			.filter(product -> product.productId() != null)
			.collect(Collectors.toMap(
				FrequentPurchaseProductResponse::productId,
				PurchaseHistoryContext::purchaseWeight,
				Long::sum
			));

		Map<Long, Long> categoryQuantityMap = safeProducts.stream()
			.filter(product -> product.categoryId() != null)
			.collect(Collectors.toMap(
				FrequentPurchaseProductResponse::categoryId,
				PurchaseHistoryContext::purchaseWeight,
				Long::sum
			));

		return new PurchaseHistoryContext(
			safeProducts,
			productQuantityMap,
			categoryQuantityMap,
			maxValue(productQuantityMap),
			maxValue(categoryQuantityMap)
		);
	}

	public boolean hasHistory() {
		return !products.isEmpty();
	}

	public Set<Long> purchasedProductIds() {
		return purchaseQuantityByProductId.keySet();
	}

	public double productAffinity(ProductSnapshot product) {
		Long purchaseQuantity = purchaseQuantityByProductId.get(product.getProductId());

		if (purchaseQuantity == null || maxProductPurchaseQuantity <= 0) {
			return 0.0;
		}

		return Math.min(purchaseQuantity.doubleValue() / maxProductPurchaseQuantity, 1.0);
	}

	public double categoryAffinity(ProductSnapshot product) {
		Long purchaseQuantity = purchaseQuantityByCategoryId.get(product.getCategoryId());

		if (purchaseQuantity == null || maxCategoryPurchaseQuantity <= 0) {
			return 0.0;
		}

		return Math.min(purchaseQuantity.doubleValue() / maxCategoryPurchaseQuantity, 1.0);
	}

	public String toPreferenceText() {
		if (!hasHistory()) {
			return "";
		}

		String purchasedProducts = products.stream()
			.sorted(Comparator
				.comparing(PurchaseHistoryContext::purchaseWeight, Comparator.reverseOrder())
				.thenComparing(FrequentPurchaseProductResponse::productId, Comparator.nullsLast(Long::compareTo)))
			.limit(10)
			.map(product -> "- %s %s, purchaseQuantity=%d, purchaseCount=%d".formatted(
				nullToEmpty(product.brandName()),
				nullToEmpty(product.productName()),
				defaultLong(product.purchaseQuantity()),
				defaultLong(product.purchaseCount())
			))
			.collect(Collectors.joining("\n"));

		return """
			User's recent frequent purchase history:
			%s
			Recommend products that match repeatedly purchased products, brands, and categories.
			""".formatted(purchasedProducts);
	}

	private static long purchaseWeight(FrequentPurchaseProductResponse product) {
		return defaultLong(product.purchaseQuantity()) + defaultLong(product.purchaseCount());
	}

	private static long maxValue(Map<Long, Long> map) {
		return map.values()
			.stream()
			.mapToLong(Long::longValue)
			.max()
			.orElse(0L);
	}

	private static long defaultLong(Long value) {
		return value == null ? 0L : value;
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
