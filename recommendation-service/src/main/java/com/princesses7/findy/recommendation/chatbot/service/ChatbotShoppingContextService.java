package com.princesses7.findy.recommendation.chatbot.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotCouponContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotProductContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotPromotionContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotShoppingContextResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.coupon.entity.CouponProductSnapshot;
import com.princesses7.findy.recommendation.coupon.repository.CouponProductSnapshotRepository;
import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;
import com.princesses7.findy.recommendation.inventory.repository.InventorySnapshotRepository;
import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;
import com.princesses7.findy.recommendation.preference.repository.CategorySnapshotRepository;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.product.repository.ProductSnapshotRepository;
import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionStatus;
import com.princesses7.findy.recommendation.promotion.repository.PromotionProductSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotShoppingContextService {

	private static final long DEFAULT_STORE_ID = 1L;
	private static final int DEFAULT_LIMIT = 5;
	private static final int MAX_LIMIT = 10;
	private static final int LOW_STOCK_THRESHOLD = 5;

	private final ProductSnapshotRepository productRepository;
	private final InventorySnapshotRepository inventoryRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final PromotionProductSnapshotRepository promotionProductRepository;
	private final CouponProductSnapshotRepository couponProductRepository;

	public ChatbotShoppingContextResponse getContext(
		ChatbotMessageRequest request,
		ChatbotIntentAnalysis analysis
	) {
		if (analysis == null || !analysis.shoppingDataRequired()) {
			return null;
		}

		Long storeId = normalizeStoreId(request.storeId());
		int limit = normalizeLimit(request.limit());
		String keyword = analysis.keyword();
		ChatIntent intent = analysis.intent();

		List<ProductSnapshot> products = findProducts(keyword, intent, limit);

		if (products.isEmpty()) {
			return ChatbotShoppingContextResponse.empty(storeId, keyword, intent);
		}

		List<Long> productIds = products.stream()
			.map(ProductSnapshot::getProductId)
			.toList();

		Map<Long, InventorySnapshot> inventoryMap = findInventoryMap(storeId, productIds);

		Map<Long, String> categoryNameMap = findCategoryNameMap(
			products.stream()
				.map(ProductSnapshot::getCategoryId)
				.toList()
		);

		Map<Long, List<PromotionProductSnapshot>> promotionMap = findPromotionMap(productIds);
		Map<Long, List<CouponProductSnapshot>> couponMap = findCouponMap(productIds);

		List<ChatbotProductContextResponse> productContexts = products.stream()
			.map(product -> toProductContext(
				product,
				categoryNameMap,
				inventoryMap,
				promotionMap,
				couponMap
			))
			.toList();

		return new ChatbotShoppingContextResponse(
			storeId,
			keyword,
			intent,
			productContexts
		);
	}

	private List<ProductSnapshot> findProducts(
		String keyword,
		ChatIntent intent,
		int limit
	) {
		if (!hasText(keyword) && intent == ChatIntent.PROMOTION_INQUIRY) {
			return findPromotionProducts(limit);
		}

		if (!hasText(keyword) && intent == ChatIntent.COUPON_INQUIRY) {
			return findCouponProducts(limit);
		}

		if (!hasText(keyword)) {
			return List.of();
		}

		List<ProductSnapshot> products = new ArrayList<>();

		products.addAll(productRepository.searchByKeyword(
			keyword,
			PageRequest.of(0, limit)
		));

		List<Long> categoryIds = categoryRepository
			.findByCategoryNameContainingIgnoreCaseAndActiveTrue(keyword)
			.stream()
			.map(CategorySnapshot::getCategoryId)
			.toList();

		if (!categoryIds.isEmpty()) {
			products.addAll(productRepository.findByCategoryIdInAndDeletedFalse(
				categoryIds,
				PageRequest.of(0, limit)
			));
		}

		return distinctRecommendableProducts(products, limit);
	}

	private List<ProductSnapshot> findPromotionProducts(int limit) {
		List<Long> productIds = promotionProductRepository.findActivePromotionProducts(
				PromotionStatus.ENDED,
				LocalDateTime.now()
			)
			.stream()
			.map(PromotionProductSnapshot::getProductId)
			.distinct()
			.limit(limit)
			.toList();

		if (productIds.isEmpty()) {
			return List.of();
		}

		return distinctRecommendableProducts(
			productRepository.findByProductIdInAndDeletedFalse(productIds),
			limit
		);
	}

	private List<ProductSnapshot> findCouponProducts(int limit) {
		List<Long> productIds = couponProductRepository.findAvailableCouponProducts(LocalDateTime.now())
			.stream()
			.map(CouponProductSnapshot::getProductId)
			.distinct()
			.limit(limit)
			.toList();

		if (productIds.isEmpty()) {
			return List.of();
		}

		return distinctRecommendableProducts(
			productRepository.findByProductIdInAndDeletedFalse(productIds),
			limit
		);
	}

	private List<ProductSnapshot> distinctRecommendableProducts(
		List<ProductSnapshot> products,
		int limit
	) {
		Map<Long, ProductSnapshot> productMap = new LinkedHashMap<>();

		for (ProductSnapshot product : products) {
			if (product != null && product.isRecommendable()) {
				productMap.putIfAbsent(product.getProductId(), product);
			}
		}

		return productMap.values()
			.stream()
			.limit(limit)
			.toList();
	}

	private Map<Long, InventorySnapshot> findInventoryMap(
		Long storeId,
		Collection<Long> productIds
	) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return inventoryRepository.findByStoreIdAndProductIdIn(storeId, productIds)
			.stream()
			.collect(Collectors.toMap(
				InventorySnapshot::getProductId,
				inventory -> inventory,
				(left, right) -> left
			));
	}

	private Map<Long, String> findCategoryNameMap(Collection<Long> categoryIds) {
		if (categoryIds.isEmpty()) {
			return Map.of();
		}

		return categoryRepository.findByCategoryIdIn(categoryIds)
			.stream()
			.filter(CategorySnapshot::isActive)
			.collect(Collectors.toMap(
				CategorySnapshot::getCategoryId,
				CategorySnapshot::getCategoryName,
				(left, right) -> left
			));
	}

	private Map<Long, List<PromotionProductSnapshot>> findPromotionMap(Collection<Long> productIds) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return promotionProductRepository.findActivePromotionProductsByProductIds(
				productIds,
				PromotionStatus.ENDED,
				LocalDateTime.now()
			)
			.stream()
			.collect(Collectors.groupingBy(PromotionProductSnapshot::getProductId));
	}

	private Map<Long, List<CouponProductSnapshot>> findCouponMap(Collection<Long> productIds) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return couponProductRepository.findAvailableCouponProductsByProductIds(
				productIds,
				LocalDateTime.now()
			)
			.stream()
			.collect(Collectors.groupingBy(CouponProductSnapshot::getProductId));
	}

	private ChatbotProductContextResponse toProductContext(
		ProductSnapshot product,
		Map<Long, String> categoryNameMap,
		Map<Long, InventorySnapshot> inventoryMap,
		Map<Long, List<PromotionProductSnapshot>> promotionMap,
		Map<Long, List<CouponProductSnapshot>> couponMap
	) {
		InventorySnapshot inventory = inventoryMap.get(product.getProductId());

		return new ChatbotProductContextResponse(
			product.getProductId(),
			product.getBrandName(),
			product.getProductName(),
			product.getCategoryId(),
			categoryNameMap.getOrDefault(product.getCategoryId(), ""),
			product.getOriginalPrice(),
			product.getSalePrice(),
			product.getDiscountRate(),
			product.getSaleStatus(),
			inventory == null ? null : inventory.getStockQuantity(),
			inventory == null ? null : inventory.getStockStatus(),
			createStockText(inventory),
			toPromotionContexts(promotionMap.getOrDefault(product.getProductId(), List.of())),
			toCouponContexts(couponMap.getOrDefault(product.getProductId(), List.of()))
		);
	}

	private List<ChatbotPromotionContextResponse> toPromotionContexts(
		List<PromotionProductSnapshot> promotionProducts
	) {
		return promotionProducts.stream()
			.map(promotionProduct -> new ChatbotPromotionContextResponse(
				promotionProduct.getPromotion().getPromotionId(),
				promotionProduct.getPromotion().getPromotionName(),
				promotionProduct.getPromotion().getPromotionType().name(),
				promotionProduct.getPromotionPrice(),
				promotionProduct.getPromotion().getBenefitText()
			))
			.toList();
	}

	private List<ChatbotCouponContextResponse> toCouponContexts(
		List<CouponProductSnapshot> couponProducts
	) {
		return couponProducts.stream()
			.map(couponProduct -> new ChatbotCouponContextResponse(
				couponProduct.getCoupon().getCouponId(),
				couponProduct.getCoupon().getCouponName(),
				couponProduct.getCoupon().getCouponType(),
				couponProduct.getCoupon().getDiscountType(),
				couponProduct.getCoupon().getDiscountValue(),
				couponProduct.getCoupon().getMinOrderAmount(),
				couponProduct.getCoupon().getBenefitText()
			))
			.toList();
	}

	private String createStockText(InventorySnapshot inventory) {
		if (inventory == null) {
			return "재고 정보 없음";
		}

		if (inventory.isOutOfStock()) {
			return "품절";
		}

		if (inventory.isLowStock()
			|| inventory.getStockQuantity() <= LOW_STOCK_THRESHOLD) {
			return "품절임박 " + inventory.getStockQuantity() + "개";
		}

		return "재고 " + inventory.getStockQuantity() + "개";
	}

	private Long normalizeStoreId(Long storeId) {
		if (storeId == null || storeId <= 0) {
			return DEFAULT_STORE_ID;
		}

		return storeId;
	}

	private int normalizeLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return DEFAULT_LIMIT;
		}

		return Math.min(limit, MAX_LIMIT);
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}