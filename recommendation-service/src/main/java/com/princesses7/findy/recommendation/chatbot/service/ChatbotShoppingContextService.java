package com.princesses7.findy.recommendation.chatbot.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;
import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotCouponContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotProductContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotPromotionContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotShoppingContextResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.repository.ShoppingProductReadRepository;
import com.princesses7.findy.recommendation.coupon.entity.CouponProductSnapshot;
import com.princesses7.findy.recommendation.coupon.repository.CouponProductSnapshotRepository;
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

	private final ShoppingProductReadRepository shoppingProductReadRepository;
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

		List<ChatbotShoppingProduct> products = findProducts(storeId, keyword, intent, limit);

		if (products.isEmpty()) {
			return ChatbotShoppingContextResponse.empty(storeId, keyword, intent);
		}

		List<Long> productIds = products.stream()
			.map(ChatbotShoppingProduct::productId)
			.toList();

		Map<Long, List<PromotionProductSnapshot>> promotionMap = findPromotionMap(productIds);
		Map<Long, List<CouponProductSnapshot>> couponMap = findCouponMap(productIds);

		List<ChatbotProductContextResponse> productContexts = products.stream()
			.map(product -> toProductContext(
				product,
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

	public List<ChatbotProductContextResponse> getProductContextsByKeyword(
		Long storeId,
		String keyword,
		int limit
	) {
		Long resolvedStoreId = normalizeStoreId(storeId);
		int resolvedLimit = normalizeLimit(limit);

		List<ChatbotShoppingProduct> products = findProducts(
			resolvedStoreId,
			keyword,
			ChatIntent.PRODUCT_SEARCH,
			resolvedLimit
		);

		if (products.isEmpty()) {
			return List.of();
		}

		List<Long> productIds = products.stream()
			.map(ChatbotShoppingProduct::productId)
			.toList();

		Map<Long, List<PromotionProductSnapshot>> promotionMap = findPromotionMap(productIds);
		Map<Long, List<CouponProductSnapshot>> couponMap = findCouponMap(productIds);

		return products.stream()
			.map(product -> toProductContext(
				product,
				promotionMap,
				couponMap
			))
			.toList();
	}

	private List<ChatbotShoppingProduct> findProducts(
		Long storeId,
		String keyword,
		ChatIntent intent,
		int limit
	) {
		if (!hasText(keyword) && intent == ChatIntent.PROMOTION_INQUIRY) {
			return findPromotionProducts(storeId, limit);
		}

		if (!hasText(keyword) && intent == ChatIntent.COUPON_INQUIRY) {
			return findCouponProducts(storeId, limit);
		}

		if (!hasText(keyword)) {
			return List.of();
		}

		return distinctRecommendableProducts(
			shoppingProductReadRepository.searchByKeyword(
				keyword,
				storeId,
				limit
			),
			limit
		);
	}

	private List<ChatbotShoppingProduct> findPromotionProducts(
		Long storeId,
		int limit
	) {
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
			shoppingProductReadRepository.findByProductIds(productIds, storeId, limit),
			limit
		);
	}

	private List<ChatbotShoppingProduct> findCouponProducts(
		Long storeId,
		int limit
	) {
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
			shoppingProductReadRepository.findByProductIds(productIds, storeId, limit),
			limit
		);
	}

	private List<ChatbotShoppingProduct> distinctRecommendableProducts(
		List<ChatbotShoppingProduct> products,
		int limit
	) {
		Map<Long, ChatbotShoppingProduct> productMap = new LinkedHashMap<>();

		for (ChatbotShoppingProduct product : products) {
			if (product != null && product.isRecommendable()) {
				productMap.putIfAbsent(product.productId(), product);
			}
		}

		return productMap.values()
			.stream()
			.limit(limit)
			.toList();
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
		ChatbotShoppingProduct product,
		Map<Long, List<PromotionProductSnapshot>> promotionMap,
		Map<Long, List<CouponProductSnapshot>> couponMap
	) {
		return new ChatbotProductContextResponse(
			product.productId(),
			product.brandName(),
			product.productName(),
			product.categoryId(),
			product.categoryName(),
			product.originalPrice(),
			product.salePrice(),
			product.discountRate(),
			product.saleStatus(),
			product.stockQuantity(),
			product.stockStatus(),
			createStockText(product),
			toPromotionContexts(promotionMap.getOrDefault(product.productId(), List.of())),
			toCouponContexts(couponMap.getOrDefault(product.productId(), List.of()))
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

	private String createStockText(ChatbotShoppingProduct product) {
		if (product.stockQuantity() == null) {
			return "재고 정보 없음";
		}

		if (product.stockQuantity() <= 0 || "OUT_OF_STOCK".equals(product.stockStatus())) {
			return "품절";
		}

		if (product.stockQuantity() <= LOW_STOCK_THRESHOLD || "LOW_STOCK".equals(product.stockStatus())) {
			return "품절임박 " + product.stockQuantity() + "개";
		}

		return "재고 " + product.stockQuantity() + "개";
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