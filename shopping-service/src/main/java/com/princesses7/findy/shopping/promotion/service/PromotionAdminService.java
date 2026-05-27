package com.princesses7.findy.shopping.promotion.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;
import com.princesses7.findy.shopping.promotion.dto.request.AddPromotionProductRequest;
import com.princesses7.findy.shopping.promotion.dto.request.CreatePromotionRequest;
import com.princesses7.findy.shopping.promotion.dto.request.UpdatePromotionRequest;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionDetailResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionPageResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionProductResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionResponse;
import com.princesses7.findy.shopping.promotion.entity.Promotion;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.exception.PromotionException;
import com.princesses7.findy.shopping.promotion.repository.PromotionProductRepository;
import com.princesses7.findy.shopping.promotion.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionAdminService {

	private final PromotionRepository promotionRepository;
	private final PromotionProductRepository promotionProductRepository;
	private final ProductRepository productRepository;

	@Transactional
	public PromotionResponse createPromotion(CreatePromotionRequest request) {
		Promotion promotion = Promotion.create(
			request.promotionName(),
			request.promotionType(),
			request.minPurchaseAmount(),
			request.buyQuantity(),
			request.getQuantity(),
			request.giftItem(),
			request.discountRate(),
			request.startAt(),
			request.endAt()
		);

		Promotion savedPromotion = promotionRepository.save(promotion);

		return PromotionResponse.from(savedPromotion);
	}

	public PromotionPageResponse getPromotions(
		String keyword,
		PromotionStatus status,
		int page,
		int size
	) {
		validatePageRequest(page, size);

		Pageable pageable = PageRequest.of(page, size);
		String normalizedKeyword = normalizeKeyword(keyword);

		Page<Promotion> promotions = findPromotions(
			normalizedKeyword,
			status,
			pageable
		);

		return PromotionPageResponse.from(promotions.map(PromotionResponse::from));
	}

	public PromotionDetailResponse getPromotion(Long promotionId) {
		Promotion promotion = getPromotionEntity(promotionId);
		List<PromotionProduct> promotionProducts = promotionProductRepository
			.findAllByPromotion_PromotionId(promotionId);

		return PromotionDetailResponse.of(promotion, promotionProducts);
	}

	@Transactional
	public PromotionResponse updatePromotion(
		Long promotionId,
		UpdatePromotionRequest request
	) {
		Promotion promotion = getPromotionEntity(promotionId);
		validateUpdatable(promotion);

		promotion.update(
			request.promotionName(),
			request.promotionType(),
			request.minPurchaseAmount(),
			request.buyQuantity(),
			request.getQuantity(),
			request.giftItem(),
			request.discountRate(),
			request.startAt(),
			request.endAt()
		);

		return PromotionResponse.from(promotion);
	}

	@Transactional
	public void endPromotion(Long promotionId) {
		Promotion promotion = getPromotionEntity(promotionId);

		promotion.end();
	}

	@Transactional
	public PromotionProductResponse addPromotionProduct(
		Long promotionId,
		AddPromotionProductRequest request
	) {
		Promotion promotion = getPromotionEntity(promotionId);
		validateProductExists(request.productId());
		validateNotDuplicateProduct(promotionId, request.productId());

		PromotionProduct promotionProduct = PromotionProduct.create(
			promotion,
			request.productId(),
			request.promotionPrice(),
			request.gridId()
		);

		PromotionProduct savedPromotionProduct = promotionProductRepository.save(promotionProduct);

		return PromotionProductResponse.from(savedPromotionProduct);
	}

	@Transactional
	public void removePromotionProduct(
		Long promotionId,
		Long promotionProductId
	) {
		getPromotionEntity(promotionId);

		PromotionProduct promotionProduct = promotionProductRepository
			.findByPromotionProductId(promotionProductId)
			.orElseThrow(() -> new PromotionException(PROMOTION_PRODUCT_NOT_FOUND));

		if (!promotionProduct.belongsTo(promotionId)) {
			throw new PromotionException(PROMOTION_PRODUCT_ACCESS_DENIED);
		}

		promotionProductRepository.delete(promotionProduct);
	}

	private Page<Promotion> findPromotions(
		String keyword,
		PromotionStatus status,
		Pageable pageable
	) {
		boolean hasKeyword = keyword != null;

		if (hasKeyword && status != null) {
			return promotionRepository.findByPromotionNameContainingIgnoreCaseAndStatus(
				keyword,
				status,
				pageable
			);
		}

		if (hasKeyword) {
			return promotionRepository.findByPromotionNameContainingIgnoreCase(
				keyword,
				pageable
			);
		}

		if (status != null) {
			return promotionRepository.findByStatus(
				status,
				pageable
			);
		}

		return promotionRepository.findAll(pageable);
	}

	private Promotion getPromotionEntity(Long promotionId) {
		return promotionRepository.findByPromotionId(promotionId)
			.orElseThrow(() -> new PromotionException(PROMOTION_NOT_FOUND));
	}

	private void validateProductExists(Long productId) {
		productRepository.findByProductIdAndIsDeletedFalse(productId)
			.orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
	}

	private void validateNotDuplicateProduct(
		Long promotionId,
		Long productId
	) {
		boolean exists = promotionProductRepository
			.existsByPromotion_PromotionIdAndProductId(
				promotionId,
				productId
			);

		if (exists) {
			throw new PromotionException(PROMOTION_PRODUCT_ALREADY_EXISTS);
		}
	}

	private void validateUpdatable(Promotion promotion) {
		if (promotion.calculateStatus(LocalDateTime.now()) == PromotionStatus.ENDED) {
			throw new PromotionException(PROMOTION_ALREADY_ENDED);
		}
	}

	private void validatePageRequest(
		int page,
		int size
	) {
		if (page < 0 || size < 1 || size > 100) {
			throw new BaseException(INVALID_INPUT_VALUE);
		}
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}

		return keyword.trim();
	}
}