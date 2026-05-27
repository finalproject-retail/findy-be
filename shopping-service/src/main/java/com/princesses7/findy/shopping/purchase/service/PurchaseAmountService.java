package com.princesses7.findy.shopping.purchase.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.repository.ProductRepository;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionDiscountResult;
import com.princesses7.findy.shopping.promotion.service.PromotionDiscountService;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseAmountItemResponse;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseAmountResponse;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseTargetItemResponse;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseTargetResponse;
import com.princesses7.findy.shopping.purchase.exception.PurchaseException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseAmountService {

	private final PurchaseTargetService purchaseTargetService;
	private final ProductRepository productRepository;
	private final PromotionDiscountService promotionDiscountService;

	public PurchaseAmountResponse calculate(Long userId) {
		PurchaseTargetResponse targetResponse = purchaseTargetService.getPurchaseTargets(userId);

		List<PurchaseAmountItemResponse> items = targetResponse.items().stream()
			.map(this::calculateItem)
			.toList();

		int totalAmount = items.stream()
			.mapToInt(PurchaseAmountItemResponse::totalAmount)
			.sum();

		int discountAmount = items.stream()
			.mapToInt(PurchaseAmountItemResponse::discountAmount)
			.sum();

		int finalAmount = items.stream()
			.mapToInt(PurchaseAmountItemResponse::finalAmount)
			.sum();

		return new PurchaseAmountResponse(
			userId,
			targetResponse.shoppingListId(),
			totalAmount,
			discountAmount,
			finalAmount,
			items
		);
	}

	private PurchaseAmountItemResponse calculateItem(PurchaseTargetItemResponse item) {
		Product product = productRepository.findByProductIdAndIsDeletedFalse(item.productId())
			.orElseThrow(() -> new PurchaseException(PRODUCT_NOT_FOUND));

		int productPrice = product.getSalePrice();
		int totalAmount = productPrice * item.purchaseQuantity();

		PromotionDiscountResult promotionDiscount = promotionDiscountService.calculateBestDiscount(
			item.productId(),
			item.purchaseQuantity(),
			productPrice,
			totalAmount
		);

		int discountAmount = promotionDiscount.discountAmount();
		int finalAmount = totalAmount - discountAmount;

		return new PurchaseAmountItemResponse(
			item.productId(),
			item.purchaseQuantity(),
			productPrice,
			totalAmount,
			discountAmount,
			finalAmount,
			promotionDiscount.promotionId(),
			promotionDiscount.promotionName(),
			promotionDiscount.promotionType(),
			promotionDiscount.benefitText()
		);
	}
}